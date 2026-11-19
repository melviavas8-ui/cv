package dev.cloudvisuals.render.gpu;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuBuffer.MappedView;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.ShapeIndexBuffer;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import dev.cloudvisuals.CloudVisualsClient;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.BuiltBuffer.DrawParameters;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class GuiDraw {
   private static final Matrix4f GUI_VIEW = new Matrix4f().setTranslation(0.0F, 0.0F, -11000.0F);
   private static final Vector4f WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   private static final Vector3f ZERO = new Vector3f();
   private static final Matrix4f IDENTITY = new Matrix4f();
   public static final RenderPipeline MARKER = RenderPipeline.builder(new Snippet[0])
      .withLocation(Identifier.of("cloudvisuals", "pipeline/gui_marker"))
      .withVertexShader("core/position")
      .withFragmentShader("core/position")
      .withVertexFormat(VertexFormats.POSITION, DrawMode.QUADS)
      .build();
   private static final Int2ObjectSortedMap<List<GuiDraw.Element>> runs = new Int2ObjectAVLTreeMap();
   private static List<GuiDraw.Element> pending = new ArrayList<>();
   private static final Map<VertexFormat, MappableRingBuffer> vertexBuffers = new HashMap<>();
   private static final Map<VertexFormat, MappableRingBuffer> usedBuffers = new IdentityHashMap<>();
   private static boolean uploaded;
   private static boolean failed;

   private GuiDraw() {
   }

   public static Matrix4f pose(DrawContext g) {
      return pose(g.getMatrices());
   }

   public static Matrix4f pose(Matrix3x2fc m) {
      return new Matrix4f(m.m00(), m.m01(), 0.0F, 0.0F, m.m10(), m.m11(), 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, m.m20(), m.m21(), 0.0F, 1.0F);
   }

   public static void scissor(DrawContext g, int x0, int y0, int x1, int y1) {
      g.getMatrices().pushMatrix();
      g.getMatrices().identity();
      g.enableScissor(x0, y0, x1, y1);
      g.getMatrices().popMatrix();
   }

   public static BufferBuilder begin(CloudVisualsShader shader) {
      return Tessellator.getInstance().begin(shader.mode(), shader.format());
   }

   public static void submit(DrawContext g, CloudVisualsShader shader, Blend blend, BufferBuilder buffer) {
      submit(g, shader, blend, buffer, null);
   }

   public static void submit(DrawContext g, CloudVisualsShader shader, Blend blend, BufferBuilder buffer, @Nullable GuiDraw.Prepare prepare) {
      BuiltBuffer mesh = buffer.endNullable();
      if (mesh != null) {
         BuiltBuffer var6 = mesh;

         try {
            submit(g, shader, blend, mesh, prepare);
         } catch (Throwable var10) {
            if (var6 != null) {
               try {
                  var6.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (var6 != null) {
            var6.close();
         }
      }
   }

   public static void submit(DrawContext g, CloudVisualsShader shader, Blend blend, BuiltBuffer mesh, @Nullable GuiDraw.Prepare prepare) {
      DrawParameters state = mesh.getDrawParameters();
      if (state.format() == shader.format() && state.mode() == shader.mode()) {
         ByteBuffer source = mesh.getBuffer();
         int vertexSize = state.format().getVertexSize();
         byte[] vertices = new byte[source.remaining()];
         source.get(source.position(), vertices);
         ByteBuffer read = ByteBuffer.wrap(vertices).order(ByteOrder.nativeOrder());
         float minX = Float.MAX_VALUE;
         float minY = Float.MAX_VALUE;
         float maxX = -Float.MAX_VALUE;
         float maxY = -Float.MAX_VALUE;

         for (int offset = 0; offset < vertices.length; offset += vertexSize) {
            float x = read.getFloat(offset);
            float y = read.getFloat(offset + 4);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
         }

         if (maxX > minX && maxY > minY) {
            int left = (int)Math.floor(minX);
            int top = (int)Math.floor(minY);
            ScreenRect bounds = new ScreenRect(left, top, Math.max(1, (int)Math.ceil(maxX) - left), Math.max(1, (int)Math.ceil(maxY) - top));
            ScreenRect scissor = g.scissorStack.peekLast();
            if (scissor != null) {
               bounds = scissor.intersection(bounds);
               if (bounds == null) {
                  return;
               }
            }

            g.state
               .addSimpleElement(
                  new GuiDraw.Element(
                     shader,
                     blend,
                     vertices,
                     state.vertexCount(),
                     state.indexCount(),
                     shader.snapshotValues(),
                     shader.snapshotTextures(),
                     scissor,
                     bounds,
                     prepare
                  )
               );
         }
      } else {
         throw new IllegalArgumentException("Mesh " + state.format() + "/" + state.mode() + " does not match shader " + shader.name());
      }
   }

   public static void onRecordMesh(RenderPipeline pipeline, int drawIndex) {
      if (pipeline == MARKER && !pending.isEmpty()) {
         runs.put(drawIndex, pending);
         pending = new ArrayList<>();
      }
   }

   @Nullable
   public static List<GuiDraw.Element> runAt(int drawIndex) {
      return runs.isEmpty() ? null : (List)runs.get(drawIndex);
   }

   public static boolean hasRuns() {
      return !runs.isEmpty();
   }

   public static void onGuiRendered() {
      runs.clear();
      pending.clear();
      uploaded = false;
   }

   public static void endFrame() {
      for (MappableRingBuffer buffer : usedBuffers.values()) {
         buffer.rotate();
      }

      usedBuffers.clear();
   }

   public static void execute(List<GuiDraw.Element> run, Framebuffer target) {
      if (!failed) {
         try {
            uploadFrame();
            draw(run, target);
         } catch (RuntimeException e) {
            failed = true;
            CloudVisualsClient.LOGGER.error("[Cloud Visuals] GUI drawing disabled", e);
         }
      }
   }

   private static void uploadFrame() {
      if (!uploaded) {
         uploaded = true;
         Object2IntOpenHashMap<VertexFormat> sizes = new Object2IntOpenHashMap();
         Object2IntOpenHashMap<DrawMode> indices = new Object2IntOpenHashMap();
         Iterator encoder = runs.values().iterator();

         while (encoder.hasNext()) {
            List<GuiDraw.Element> run = (List<GuiDraw.Element>)encoder.next();

            for (GuiDraw.Element e : run) {
               sizes.addTo(e.shader.format(), e.vertices.length);
               indices.addTo(e.shader.mode(), e.indexCount);
            }
         }

         CommandEncoder encoderx = RenderSystem.getDevice().createCommandEncoder();
         Iterator var18 = sizes.object2IntEntrySet().iterator();

         while (var18.hasNext()) {
            Entry<VertexFormat> entry = (Entry<VertexFormat>)var18.next();
            VertexFormat format = (VertexFormat)entry.getKey();
            int size = entry.getIntValue();
            MappableRingBuffer buffer = vertexBuffers.get(format);
            if (buffer == null || buffer.size() < size) {
               if (buffer != null) {
                  buffer.close();
               }

               int capacity = Math.max(65536, Integer.highestOneBit(size - 1) << 1);
               buffer = new MappableRingBuffer(() -> "Cloud Visuals GUI vertices " + format, 34, capacity);
               vertexBuffers.put(format, buffer);
            }

            usedBuffers.put(format, buffer);
            MappedView view = encoderx.mapBuffer(buffer.getBlocking().slice(0L, size), false, true);

            try {
               ByteBuffer data = view.data();
               int offset = 0;
               Iterator var11 = runs.values().iterator();

               while (var11.hasNext()) {
                  List<GuiDraw.Element> run = (List<GuiDraw.Element>)var11.next();

                  for (GuiDraw.Element e : run) {
                     if (e.shader.format() == format) {
                        data.put(offset, e.vertices);
                        e.baseVertex = offset / format.getVertexSize();
                        offset += e.vertices.length;
                     }
                  }
               }
            } catch (Throwable var16) {
               if (view != null) {
                  try {
                     view.close();
                  } catch (Throwable var15) {
                     var16.addSuppressed(var15);
                  }
               }

               throw var16;
            }

            if (view != null) {
               view.close();
            }
         }

         Iterator var20 = indices.object2IntEntrySet().iterator();

         while (var20.hasNext()) {
            Entry<DrawMode> entry = (Entry<DrawMode>)var20.next();
            if (entry.getIntValue() > 0) {
               RenderSystem.getSequentialBuffer((DrawMode)entry.getKey()).getIndexBuffer(entry.getIntValue());
            }
         }
      }
   }

   private static void draw(List<GuiDraw.Element> run, Framebuffer target) {
      CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
      GpuTextureView color = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : target.getColorAttachmentView();
      int i = 0;
      int n = run.size();

      while (i < n) {
         GuiDraw.Element head = run.get(i);
         if (head.prepare != null) {
            head.prepare.run(head);
         }

         int end = i + 1;

         while (end < n && run.get(end).prepare == null) {
            end++;
         }

         boolean any = false;

         for (int k = i; k < end; k++) {
            GuiDraw.Element e = run.get(k);
            if (!e.skipped && valid(e)) {
               e.params = e.shader.upload(e.values);
               any = true;
            } else {
               e.skipped = true;
            }
         }

         if (any) {
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().write(GUI_VIEW, WHITE, ZERO, IDENTITY);
            RenderPass pass = encoder.createRenderPass(() -> "Cloud Visuals GUI", color, OptionalInt.empty());

            try {
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("DynamicTransforms", transforms);
               int k = i;

               while (k < end) {
                  GuiDraw.Element first = run.get(k);
                  if (first.skipped) {
                     k++;
                  } else {
                     int indexCount = first.indexCount;

                     int m;
                     for (m = k + 1; m < end && mergeable(run.get(m - 1), run.get(m)); m++) {
                        indexCount += run.get(m).indexCount;
                     }

                     drawGroup(pass, first, indexCount);
                     k = m;
                  }
               }
            } catch (Throwable var16) {
               if (pass != null) {
                  try {
                     pass.close();
                  } catch (Throwable var15) {
                     var16.addSuppressed(var15);
                  }
               }

               throw var16;
            }

            if (pass != null) {
               pass.close();
            }
         }

         i = end;
      }
   }

   private static boolean mergeable(GuiDraw.Element a, GuiDraw.Element b) {
      DrawMode mode = a.shader.mode();
      return !b.skipped
         && !a.skipped
         && a.shader == b.shader
         && a.blend == b.blend
         && (mode == DrawMode.QUADS || mode == DrawMode.TRIANGLES)
         && b.baseVertex == a.baseVertex + a.vertexCount
         && Objects.equals(a.scissor, b.scissor)
         && Objects.equals(a.params, b.params)
         && a.textures.equals(b.textures);
   }

   private static void drawGroup(RenderPass pass, GuiDraw.Element e, int indexCount) {
      pass.setPipeline(e.shader.pipeline(e.blend));
      if (e.params != null) {
         pass.setUniform("CloudVisualsParams", e.params);
      }

      for (java.util.Map.Entry<String, CloudVisualsShader.Texture> texture : e.textures.entrySet()) {
         pass.bindTexture(texture.getKey(), texture.getValue().view(), texture.getValue().sampler());
      }

      if (e.scissor != null) {
         enableScissor(e.scissor, pass);
      } else {
         pass.disableScissor();
      }

      pass.setVertexBuffer(0, vertexBuffers.get(e.shader.format()).getBlocking());
      ShapeIndexBuffer indices = RenderSystem.getSequentialBuffer(e.shader.mode());
      pass.setIndexBuffer(indices.getIndexBuffer(indexCount), indices.getIndexType());
      pass.drawIndexed(e.baseVertex, 0, indexCount, 1);
   }

   private static boolean valid(GuiDraw.Element e) {
      return e.shader.ready(e.blend) && e.textures.keySet().containsAll(e.shader.samplers());
   }

   static void enableScissor(ScreenRect rectangle, RenderPass pass) {
      Window window = MinecraftClient.getInstance().getWindow();
      int scale = window.getScaleFactor();
      int x = rectangle.getLeft() * scale;
      int y = window.getFramebufferHeight() - rectangle.getBottom() * scale;
      pass.enableScissor(x, y, Math.max(0, rectangle.width() * scale), Math.max(0, rectangle.height() * scale));
   }

   public static final class Element implements SimpleGuiElementRenderState {
      final CloudVisualsShader shader;
      final Blend blend;
      final byte[] vertices;
      final int vertexCount;
      final int indexCount;
      final ByteBuffer values;
      final Map<String, CloudVisualsShader.Texture> textures;
      @Nullable
      final ScreenRect scissor;
      final ScreenRect bounds;
      @Nullable
      final GuiDraw.Prepare prepare;
      int baseVertex;
      boolean skipped;
      @Nullable
      GpuBufferSlice params;

      Element(
         CloudVisualsShader shader,
         Blend blend,
         byte[] vertices,
         int vertexCount,
         int indexCount,
         ByteBuffer values,
         Map<String, CloudVisualsShader.Texture> textures,
         @Nullable ScreenRect scissor,
         ScreenRect bounds,
         @Nullable GuiDraw.Prepare prepare
      ) {
         this.shader = shader;
         this.blend = blend;
         this.vertices = vertices;
         this.vertexCount = vertexCount;
         this.indexCount = indexCount;
         this.values = values;
         this.textures = prepare == null ? textures : new LinkedHashMap<>(textures);
         this.scissor = scissor;
         this.bounds = bounds;
         this.prepare = prepare;
      }

      public void texture(String name, GpuTextureView view, GpuSampler sampler) {
         this.textures.put(name, new CloudVisualsShader.Texture(view, sampler));
      }

      public void skip() {
         this.skipped = true;
      }

      public ScreenRect area() {
         return this.bounds;
      }

      public void setupVertices(VertexConsumer vertices) {
         GuiDraw.pending.add(this);

         for (int i = 0; i < 4; i++) {
            vertices.vertex(0.0F, 0.0F, 0.0F);
         }
      }

      public RenderPipeline pipeline() {
         return GuiDraw.MARKER;
      }

      public TextureSetup textureSetup() {
         return TextureSetup.empty();
      }

      @Nullable
      public ScreenRect scissorArea() {
         return this.scissor;
      }

      public ScreenRect bounds() {
         return this.bounds;
      }
   }

   @FunctionalInterface
   public interface Prepare {
      void run(GuiDraw.Element var1);
   }
}
