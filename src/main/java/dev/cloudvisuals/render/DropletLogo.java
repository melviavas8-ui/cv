package dev.cloudvisuals.render;

import dev.cloudvisuals.CloudVisualsClient;
import dev.cloudvisuals.render.gpu.CloudVisualsTexture;
import dev.cloudvisuals.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

public final class DropletLogo {
   public static final float ASPECT = 1.34F;
   private static final long FRAME_NANOS = 14000000L;
   private final String name;
   private CloudVisualsTexture texture;
   private Identifier id;
   private int width;
   private int height;
   private float[] coverage;
   private float[] below;
   private float[] above;
   private float[] surfaceFront;
   private float[] surfaceBack;
   private float bottom;
   private float top;
   private long lastPaint;
   private float lastLevel = -1.0F;
   private int lastColors;

   public DropletLogo(String name) {
      this.name = name;
   }

   public void draw(DrawContext g, float x, float y, float width, float level, float alpha) {
      if (!(alpha <= 0.003F) && !(width <= 1.0F)) {
         double scale = MinecraftClient.getInstance().getWindow().getScaleFactor();
         int pw = Math.max(8, Math.round((float)(width * scale)));
         int ph = Math.round(pw * 1.34F);
         this.ensure(pw, ph);
         if (this.texture != null) {
            long now = System.nanoTime();
            int colors = Theme.accent() * 31 + Theme.accent2();
            if (now - this.lastPaint >= 14000000L || Math.abs(level - this.lastLevel) > 0.002F || colors != this.lastColors) {
               this.lastPaint = now;
               this.lastLevel = level;
               this.lastColors = colors;
               this.paint(Math.clamp(level, 0.0F, 1.0F), Theme.time());
               this.texture.upload();
            }

            float px = (float)(Math.round(x * scale) / scale);
            float py = (float)(Math.round(y * scale) / scale);
            g.getMatrices().pushMatrix();
            g.getMatrices().translate(px, py);
            g.getMatrices().scale((float)(1.0 / scale), (float)(1.0 / scale));
            g.drawTexture(RenderPipelines.GUI_TEXTURED, this.id, 0, 0, 0.0F, 0.0F, pw, ph, pw, ph, pw, ph, ColorUtil.fade(-1, alpha));
            g.getMatrices().popMatrix();
         }
      }
   }

   public void close() {
      if (this.id != null) {
         MinecraftClient.getInstance().getTextureManager().destroyTexture(this.id);
      }

      this.texture = null;
      this.id = null;
      this.width = this.height = 0;
   }

   private void ensure(int pw, int ph) {
      if (this.texture == null || pw != this.width || ph != this.height) {
         this.close();

         try {
            NativeImage image = new NativeImage(pw, ph, true);
            this.texture = new CloudVisualsTexture("droplet " + this.name, image).filter(false, false);
            this.id = Identifier.of("cloudvisuals", "dynamic/droplet_" + this.name);
            MinecraftClient.getInstance().getTextureManager().registerTexture(this.id, this.texture);
         } catch (RuntimeException e) {
            CloudVisualsClient.LOGGER.error("[Cloud Visuals] droplet texture", e);
            this.texture = null;
            return;
         }

         this.width = pw;
         this.height = ph;
         this.lastLevel = -1.0F;
         this.prepare();
      }
   }

   private void prepare() {
      int n = this.width * this.height;
      this.coverage = new float[n];
      this.below = new float[n * 4];
      this.above = new float[n * 4];
      this.surfaceFront = new float[this.width];
      this.surfaceBack = new float[this.width];
      float pad = Math.max(1.5F, this.width * 0.02F);
      float radius = this.width / 2.0F - pad;
      float cx = this.width / 2.0F;
      float cy = this.height - pad - radius;
      float tipY = pad;
      float round = this.width * 0.06F;
      this.bottom = cy + radius;
      this.top = tipY;
      float innerRadius = radius - round;
      float h = Math.max(cy - (tipY + round), innerRadius + 1.0F);
      float side = (float)Math.sqrt(h * h - innerRadius * innerRadius);
      float sinB = side / h;
      float cosB = innerRadius / h;
      float tangentX = innerRadius * sinB;
      float tangentY = -innerRadius * cosB;
      float dirX = tangentX / side;
      float dirY = (tangentY + h) / side;

      for (int py = 0; py < this.height; py++) {
         for (int px = 0; px < this.width; px++) {
            float x = Math.abs(px + 0.5F - cx);
            float y = py + 0.5F - cy;
            float inner;
            float nx;
            float ny;
            if (y > tangentY) {
               float len = (float)Math.sqrt(x * x + y * y);
               inner = len - innerRadius;
               nx = len > 0.0F ? x / len : 0.0F;
               ny = len > 0.0F ? y / len : 1.0F;
            } else if (x * dirX + (y + h) * dirY < 0.0F) {
               float dy = y + h;
               float len = (float)Math.sqrt(x * x + dy * dy);
               inner = len;
               nx = len > 0.0F ? x / len : 0.0F;
               ny = len > 0.0F ? dy / len : -1.0F;
            } else {
               inner = x * dirY - (y + h) * dirX;
               nx = dirY;
               ny = -dirX;
            }

            float sd = inner - round;
            float cov = Math.clamp(0.5F - sd, 0.0F, 1.0F);
            int i = py * this.width + px;
            this.coverage[i] = cov;
            if (!(cov <= 0.0F)) {
               if (px + 0.5F < cx) {
                  nx = -nx;
               }

               float depth = -sd;
               float t = Math.clamp((py + 0.5F - this.top) / (this.bottom - this.top), 0.0F, 1.0F);
               float bodyA = 0.13F - 0.06F * t;
               put(this.below, i, 1.0F * bodyA, 1.0F * bodyA, 1.0F * bodyA, bodyA);
               float r = 0.0F;
               float g = 0.0F;
               float b = 0.0F;
               float a = 0.0F;
               float shade = (float)Math.pow(Math.clamp(1.0F - depth / (radius * 0.28F), 0.0F, 1.0F), 2.0) * Math.max(0.0F, ny) * 0.3F;
               a = over(a, shade);
               r *= 1.0F - shade;
               g *= 1.0F - shade;
               b *= 1.0F - shade;
               float light = Math.clamp(0.3F + 0.7F * Math.max(0.0F, -(nx * 0.55F + ny * 0.83F)), 0.0F, 1.0F);
               float rimWidth = Math.max(0.8F, this.width * 0.012F);
               float rimT = (depth - rimWidth * 0.9F) / rimWidth;
               float rim = (float)Math.exp(-rimT * rimT) * (0.22F + 0.58F * light);
               r = r * (1.0F - rim) + rim;
               g = g * (1.0F - rim) + rim;
               b = b * (1.0F - rim) + rim;
               a = over(a, rim);
               float sx = px + 0.5F - (cx - radius * 0.4F);
               float sy = py + 0.5F - (cy - radius * 0.28F);
               float cos = 0.82F;
               float sin = 0.57F;
               float u = (sx * cos + sy * sin) / (radius * 0.11F);
               float v = (-sx * sin + sy * cos) / (radius * 0.27F);
               float spec = (float)Math.pow(Math.clamp(1.0F - (u * u + v * v), 0.0F, 1.0F), 1.6) * 0.5F;
               r = r * (1.0F - spec) + spec;
               g = g * (1.0F - spec) + spec;
               b = b * (1.0F - spec) + spec;
               a = over(a, spec);
               put(this.above, i, r, g, b, a);
            }
         }
      }
   }

   private static float over(float dst, float src) {
      return src + dst * (1.0F - src);
   }

   private static void put(float[] layer, int i, float r, float g, float b, float a) {
      int o = i * 4;
      layer[o] = r;
      layer[o + 1] = g;
      layer[o + 2] = b;
      layer[o + 3] = a;
   }

   private void paint(float level, float time) {
      NativeImage image = this.texture.getImage();
      if (image != null) {
         float eased = level <= 0.0F ? 0.0F : (level >= 1.0F ? 1.0F : (float)Math.pow(level, 0.85));
         float surface = this.bottom - eased * (this.bottom - this.top);
         float amp = this.width * 0.03F * Math.clamp(Math.min(eased, 1.0F - eased) * 7.0F, 0.0F, 1.0F);
         float tau = (float) (Math.PI * 2);

         for (int px = 0; px < this.width; px++) {
            float x = (px + 0.5F) / this.width;
            float w1 = (float)Math.sin(x * tau * 1.15F + time * 2.3F);
            float w2 = (float)Math.sin(x * tau * 2.1F - time * 1.7F + 1.3F);
            this.surfaceFront[px] = surface + amp * (0.65F * w1 + 0.35F * w2);
            this.surfaceBack[px] = surface - amp * 0.6F + amp * 0.7F * (float)Math.sin(x * tau * 0.9F - time * 1.9F + 2.4F);
         }

         int accent = Theme.accent();
         int accent2 = Theme.accent2();
         int topColor = ColorUtil.lerp(accent, -1, 0.2F);
         int bottomColor = ColorUtil.lerp(accent2, -16777216, 0.22F);
         int backColor = ColorUtil.lerp(accent2, -1, 0.1F);
         float br = ColorUtil.red(backColor) / 255.0F;
         float bg = ColorUtil.green(backColor) / 255.0F;
         float bb = ColorUtil.blue(backColor) / 255.0F;
         float span = Math.max(1.0F, this.bottom - surface);

         for (int py = 0; py < this.height; py++) {
            float y = py + 0.5F;
            float t = Math.clamp((y - surface) / span, 0.0F, 1.0F);
            int liquid = ColorUtil.lerp(topColor, bottomColor, t);
            float lr = ColorUtil.red(liquid) / 255.0F;
            float lg = ColorUtil.green(liquid) / 255.0F;
            float lb = ColorUtil.blue(liquid) / 255.0F;

            for (int px = 0; px < this.width; px++) {
               int i = py * this.width + px;
               float cov = this.coverage[i];
               if (!(cov <= 0.0F)) {
                  int o = i * 4;
                  float r = this.below[o];
                  float g = this.below[o + 1];
                  float b = this.below[o + 2];
                  float a = this.below[o + 3];
                  float back = Math.clamp(y - this.surfaceBack[px] + 0.5F, 0.0F, 1.0F) * 0.5F;
                  if (back > 0.0F) {
                     r = br * back + r * (1.0F - back);
                     g = bg * back + g * (1.0F - back);
                     b = bb * back + b * (1.0F - back);
                     a = back + a * (1.0F - back);
                  }

                  float distance = y - this.surfaceFront[px];
                  float front = Math.clamp(distance + 0.5F, 0.0F, 1.0F) * 0.94F;
                  if (front > 0.0F) {
                     r = lr * front + r * (1.0F - front);
                     g = lg * front + g * (1.0F - front);
                     b = lb * front + b * (1.0F - front);
                     a = front + a * (1.0F - front);
                  }

                  if (eased > 0.0F && eased < 1.0F) {
                     float lineWidth = Math.max(0.9F, this.width * 0.011F);
                     float d = distance / lineWidth;
                     float line = (float)Math.exp(-d * d) * 0.55F;
                     r = line + r * (1.0F - line);
                     g = line + g * (1.0F - line);
                     b = line + b * (1.0F - line);
                     a = line + a * (1.0F - line);
                  }

                  float sa = this.above[o + 3];
                  r = this.above[o] + r * (1.0F - sa);
                  g = this.above[o + 1] + g * (1.0F - sa);
                  b = this.above[o + 2] + b * (1.0F - sa);
                  a = sa + a * (1.0F - sa);
                  a *= cov;
                  if (a <= 0.002F) {
                     image.setColor(px, py, 0);
                  } else {
                     float inv = 1.0F / (a / cov);
                     int ir = Math.min(255, Math.round(r * inv * 255.0F));
                     int ig = Math.min(255, Math.round(g * inv * 255.0F));
                     int ib = Math.min(255, Math.round(b * inv * 255.0F));
                     int ia = Math.min(255, Math.round(a * 255.0F));
                     image.setColor(px, py, ia << 24 | ib << 16 | ig << 8 | ir);
                  }
               }
            }
         }
      }
   }
}
