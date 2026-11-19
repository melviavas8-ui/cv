package dev.cloudvisuals.render.gpu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import dev.cloudvisuals.CloudVisualsClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gl.DynamicUniformStorage;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gl.DynamicUniformStorage.Uploadable;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;

public final class CloudVisualsShader {
   private static final List<CloudVisualsShader> ALL = new ArrayList<>();
   private final String name;
   private final Identifier vertex;
   private final Identifier fragment;
   private final VertexFormat format;
   private final DrawMode mode;
   private final List<String> samplers;
   private final boolean screenSpace;
   private final UniformBlock block;
   private final ByteBuffer values;
   private final Map<String, CloudVisualsShader.Texture> textures = new LinkedHashMap<>();
   private final RenderPipeline[] pipelines = new RenderPipeline[Blend.values().length * CloudVisualsShader.Depth.values().length];
   @Nullable
   private DynamicUniformStorage<CloudVisualsShader.Values> storage;

   private CloudVisualsShader(String namespace, String name, String vertex, String fragment, VertexFormat format, DrawMode mode, List<String> samplers) {
      this.name = name;
      this.vertex = Identifier.of(namespace, "core/" + vertex);
      this.fragment = Identifier.of(namespace, "core/" + fragment);
      this.format = format;
      this.mode = mode;
      this.samplers = List.copyOf(samplers);
      this.screenSpace = vertex.equals("blit");
      UniformBlock parsed = UniformBlock.parse(read("core/" + fragment + ".fsh"));
      if (parsed.size() == 0) {
         parsed = UniformBlock.parse(read("core/" + vertex + ".vsh"));
      }

      this.block = parsed;
      this.values = this.block.newValues();
   }

   public static CloudVisualsShader create(String name, String vertex, String fragment, VertexFormat format, DrawMode mode, String... samplers) {
      return register(new CloudVisualsShader("cloudvisuals", name, vertex, fragment, format, mode, List.of(samplers)));
   }

   public static CloudVisualsShader vanilla(String name, String shader, VertexFormat format, DrawMode mode, String... samplers) {
      return register(new CloudVisualsShader("minecraft", name, shader, shader, format, mode, List.of(samplers)));
   }

   private static CloudVisualsShader register(CloudVisualsShader shader) {
      synchronized (ALL) {
         ALL.add(shader);
         return shader;
      }
   }

   public static CloudVisualsShader create(String name, VertexFormat format, DrawMode mode) {
      JsonObject json = JsonParser.parseString(read("core/" + name + ".json")).getAsJsonObject();
      List<String> samplers = new ArrayList<>();
      if (json.has("samplers")) {
         for (JsonElement sampler : json.getAsJsonArray("samplers")) {
            samplers.add(sampler.getAsJsonObject().get("name").getAsString());
         }
      }

      CloudVisualsShader shader = create(
         name, path(json.get("vertex").getAsString()), path(json.get("fragment").getAsString()), format, mode, samplers.toArray(String[]::new)
      );
      if (json.has("uniforms")) {
         for (JsonElement element : json.getAsJsonArray("uniforms")) {
            JsonObject uniform = element.getAsJsonObject();
            JsonArray values = uniform.getAsJsonArray("values");
            float[] numbers = new float[values.size()];

            for (int i = 0; i < numbers.length; i++) {
               numbers[i] = values.get(i).getAsFloat();
            }

            shader.safeGetUniform(uniform.get("name").getAsString()).set(numbers);
         }
      }

      return shader;
   }

   private static String path(String location) {
      int colon = location.indexOf(58);
      return colon < 0 ? location : location.substring(colon + 1);
   }

   private static Identifier id(String path) {
      return Identifier.of("cloudvisuals", path);
   }

   private static String read(String path) {
      try (InputStream in = CloudVisualsShader.class.getResourceAsStream("/assets/cloudvisuals/shaders/" + path)) {
         return in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
      } catch (IOException e) {
         CloudVisualsClient.LOGGER.error("[Cloud Visuals] cannot read shader {}", path, e);
         return "";
      }
   }

   public String name() {
      return this.name;
   }

   public VertexFormat format() {
      return this.format;
   }

   public DrawMode mode() {
      return this.mode;
   }

   public boolean screenSpace() {
      return this.screenSpace;
   }

   public List<String> samplers() {
      return this.samplers;
   }

   public boolean ready(Blend blend) {
      return this.ready(blend, CloudVisualsShader.Depth.NONE);
   }

   public boolean ready(Blend blend, CloudVisualsShader.Depth depth) {
      return RenderSystem.getDevice().precompilePipeline(this.pipeline(blend, depth)).isValid();
   }

   public RenderPipeline pipeline(Blend blend) {
      return this.pipeline(blend, CloudVisualsShader.Depth.NONE);
   }

   public RenderPipeline pipeline(Blend blend, CloudVisualsShader.Depth depth) {
      int index = blend.ordinal() * CloudVisualsShader.Depth.values().length + depth.ordinal();
      RenderPipeline pipeline = this.pipelines[index];
      if (pipeline == null) {
         this.pipelines[index] = pipeline = this.build(blend, depth);
      }

      return pipeline;
   }

   private RenderPipeline build(Blend blend, CloudVisualsShader.Depth depth) {
      Builder builder = this.screenSpace
         ? RenderPipeline.builder(new Snippet[0])
         : RenderPipeline.builder(new Snippet[]{RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET});
      builder.withLocation(
            id(
               "pipeline/"
                  + this.name
                  + "/"
                  + blend.name().toLowerCase(Locale.ROOT)
                  + (depth == CloudVisualsShader.Depth.NONE ? "" : "_" + depth.name().toLowerCase(Locale.ROOT))
            )
         )
         .withVertexShader(this.vertex)
         .withFragmentShader(this.fragment)
         .withVertexFormat(this.format, this.mode)
         .withDepthTestFunction(depth == CloudVisualsShader.Depth.NONE ? DepthTestFunction.NO_DEPTH_TEST : DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(depth == CloudVisualsShader.Depth.TEST_AND_WRITE)
         .withCull(false);
      if (this.block.size() > 0) {
         builder.withUniform("CloudVisualsParams", UniformType.UNIFORM_BUFFER);
      }

      for (String sampler : this.samplers) {
         builder.withSampler(sampler);
      }

      if (blend.function() != null) {
         builder.withBlend(blend.function());
      } else {
         builder.withoutBlend();
      }

      return builder.build();
   }

   public CloudVisualsShader.Uniform safeGetUniform(String name) {
      UniformBlock.Field field = this.block.field(name);
      return field == null ? CloudVisualsShader.Uniform.DUMMY : new CloudVisualsShader.Uniform(this.values, field);
   }

   public void setSampler(String name, GpuTextureView view, GpuSampler sampler) {
      this.textures.put(name, new CloudVisualsShader.Texture(view, sampler));
   }

   public void setSampler(String name, AbstractTexture texture) {
      this.textures.put(name, new CloudVisualsShader.Texture(texture.getGlTextureView(), texture.getSampler()));
   }

   public void clearSamplers() {
      this.textures.clear();
   }

   public ByteBuffer snapshotValues() {
      ByteBuffer copy = this.block.newValues();
      copy.put(0, this.values, 0, this.values.capacity());
      return copy;
   }

   public Map<String, CloudVisualsShader.Texture> snapshotTextures() {
      return Map.copyOf(this.textures);
   }

   Map<String, CloudVisualsShader.Texture> currentTextures() {
      return this.textures;
   }

   public int blockSize() {
      return this.block.size();
   }

   @Nullable
   GpuBufferSlice upload(ByteBuffer snapshot) {
      if (this.block.size() == 0) {
         return null;
      }

      if (this.storage == null) {
         this.storage = new DynamicUniformStorage("Cloud Visuals " + this.name, this.block.size(), 32);
      }

      return this.storage.write(new CloudVisualsShader.Values(snapshot));
   }

   public static void endFrame() {
      synchronized (ALL) {
         for (CloudVisualsShader shader : ALL) {
            if (shader.storage != null) {
               shader.storage.clear();
            }
         }
      }
   }

   public enum Depth {
      NONE,
      TEST,
      TEST_AND_WRITE;

      Depth() {
      }
   }

   public record Texture(GpuTextureView view, GpuSampler sampler) {
      public Texture {
      }
   }

   public static class Uniform {
      static final CloudVisualsShader.Uniform DUMMY = new CloudVisualsShader.Uniform(null, null);
      @Nullable
      private final ByteBuffer values;
      @Nullable
      private final UniformBlock.Field field;

      Uniform(@Nullable ByteBuffer values, @Nullable UniformBlock.Field field) {
         this.values = values;
         this.field = field;
      }

      public void set(float x) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, x);
         }
      }

      public void set(float x, float y) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, x, y);
         }
      }

      public void set(float x, float y, float z) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, x, y, z);
         }
      }

      public void set(float x, float y, float z, float w) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, x, y, z, w);
         }
      }

      public void set(float[] numbers) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, numbers);
         }
      }

      public void set(int x) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, x);
         }
      }

      public void set(int x, int y) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, x, y);
         }
      }

      public void set(int x, int y, int z, int w) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, x, y, z, w);
         }
      }

      public void set(Matrix4fc matrix) {
         if (this.field != null) {
            UniformBlock.write(this.values, this.field, matrix);
         }
      }
   }

   record Values(ByteBuffer bytes) implements Uploadable {
      Values {
      }

      public void write(ByteBuffer buffer) {
         ByteBuffer source = this.bytes.duplicate();
         source.position(0).limit(Math.min(source.capacity(), buffer.remaining()));
         buffer.put(source);
      }
   }
}
