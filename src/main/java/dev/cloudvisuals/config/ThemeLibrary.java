package dev.cloudvisuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.cloudvisuals.CloudVisualsClient;
import dev.cloudvisuals.module.Modules;
import dev.cloudvisuals.module.impl.ThemeModule;
import dev.cloudvisuals.setting.Setting;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import net.fabricmc.loader.api.FabricLoader;

public final class ThemeLibrary {
   private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("cloudvisuals").resolve("themes");
   private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
   private static final String CODE_PREFIX = "cloudvisuals-theme1:";
   private static final int MAX_CODE_BYTES = 65536;

   private ThemeLibrary() {
   }

   public static List<String> saved() {
      List<String> names = new ArrayList<>();
      if (!Files.isDirectory(DIR)) {
         return names;
      }

      try (Stream<Path> files = Files.list(DIR)) {
         files.<String>map(file -> file.getFileName().toString())
            .filter(name -> name.endsWith(".json"))
            .map(name -> name.substring(0, name.length() - 5))
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .forEach(names::add);
      } catch (IOException e) {
         CloudVisualsClient.LOGGER.error("[Cloud Visuals] cannot list themes", e);
      }

      return names;
   }

   public static JsonObject read(String name) {
      try {
         JsonElement root = JsonParser.parseString(Files.readString(file(name), StandardCharsets.UTF_8));
         return root.isJsonObject() ? root.getAsJsonObject() : null;
      } catch (IOException | RuntimeException e) {
         return null;
      }
   }

   public static void save(String name) {
      if (CloudVisualsClient.uiTest() == null) {
         try {
            Files.createDirectories(DIR);
            Files.writeString(file(name), PRETTY.toJson(current()), StandardCharsets.UTF_8);
         } catch (IOException e) {
            CloudVisualsClient.LOGGER.error("[Cloud Visuals] cannot save theme {}", name, e);
         }
      }
   }

   public static boolean load(String name) {
      JsonObject settings = read(name);
      if (settings == null) {
         return false;
      }

      apply(settings);
      return true;
   }

   public static boolean delete(String name) {
      try {
         return Files.deleteIfExists(file(name));
      } catch (IOException e) {
         return false;
      }
   }

   public static void applyBuiltin(ThemeLibrary.Builtin theme) {
      ThemeModule module = Modules.theme();
      if (module != null) {
         module.applyBuiltin(theme);
         ConfigManager.markDirty();
      }
   }

   public static String exportCode() {
      byte[] json = current().toString().getBytes(StandardCharsets.UTF_8);
      ByteArrayOutputStream packed = new ByteArrayOutputStream();

      try (DeflaterOutputStream out = new DeflaterOutputStream(packed)) {
         out.write(json);
      } catch (IOException e) {
         throw new IllegalStateException(e);
      }

      return "cloudvisuals-theme1:" + Base64.getUrlEncoder().withoutPadding().encodeToString(packed.toByteArray());
   }

   public static boolean importCode(String code) {
      String trimmed = code == null ? "" : code.trim();
      if (!trimmed.startsWith("cloudvisuals-theme1:")) {
         return false;
      }

      try {
         byte[] packed = Base64.getUrlDecoder().decode(trimmed.substring("cloudvisuals-theme1:".length()));
         InputStream in = new InflaterInputStream(new ByteArrayInputStream(packed));

         byte[] json;
         try {
            json = in.readNBytes(65537);
         } catch (Throwable var8) {
            try {
               in.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         in.close();
         if (json.length > 65536) {
            return false;
         }

         JsonElement root = JsonParser.parseString(new String(json, StandardCharsets.UTF_8));
         if (!root.isJsonObject()) {
            return false;
         }

         apply(root.getAsJsonObject());
         return true;
      } catch (IOException | RuntimeException e) {
         CloudVisualsClient.LOGGER.warn("[Cloud Visuals] theme code rejected", e);
         return false;
      }
   }

   private static JsonObject current() {
      JsonObject settings = new JsonObject();
      ThemeModule module = Modules.theme();
      if (module == null) {
         return settings;
      }

      for (Setting<?> setting : module.settings()) {
         settings.add(setting.id(), setting.toJson());
      }

      return settings;
   }

   private static void apply(JsonObject settings) {
      ThemeModule module = Modules.theme();
      if (module != null) {
         for (Setting<?> setting : module.settings()) {
            if (settings.has(setting.id())) {
               setting.fromJson(settings.get(setting.id()));
            }
         }

         module.applyAnimated();
         ConfigManager.markDirty();
      }
   }

   private static Path file(String name) {
      String clean = name.replaceAll("[^\\p{L}\\p{N} _-]", "").trim();
      return DIR.resolve((clean.isEmpty() ? "theme" : clean) + ".json");
   }

   public enum Builtin {
      VIOLET(ThemeModule.Palette.VIOLET, ThemeModule.Preset.DARK, 0, ThemeModule.LabelColor.AUTO, ThemeModule.CornerStyle.SQUIRCLE, 100, Map.of()),
      MIDNIGHT(
         ThemeModule.Palette.MIDNIGHT,
         ThemeModule.Preset.DARK,
         -1341518828,
         ThemeModule.LabelColor.AUTO,
         ThemeModule.CornerStyle.SQUIRCLE,
         110,
         Map.of("blur", 92.0, "specular", 50.0, "rim", 28.0)
      ),
      MORNING(ThemeModule.Palette.MIDNIGHT, ThemeModule.Preset.LIGHT, 0, ThemeModule.LabelColor.AUTO, ThemeModule.CornerStyle.SQUIRCLE, 100, Map.of()),
      CRYSTAL(
         ThemeModule.Palette.OCEAN, ThemeModule.Preset.CLEAR, 0, ThemeModule.LabelColor.LIGHT, ThemeModule.CornerStyle.ROUND, 120, Map.of("dispersion", 45.0)
      ),
      SAKURA(ThemeModule.Palette.SAKURA, ThemeModule.Preset.LIGHT, -1459621643, ThemeModule.LabelColor.DARK, ThemeModule.CornerStyle.ROUND, 130, Map.of()),
      SUNSET(
         ThemeModule.Palette.SUNSET,
         ThemeModule.Preset.DARK,
         -1609036792,
         ThemeModule.LabelColor.AUTO,
         ThemeModule.CornerStyle.SQUIRCLE,
         100,
         Map.of("saturation", 140.0)
      ),
      GRAPHITE(
         ThemeModule.Palette.MONO,
         ThemeModule.Preset.DARK,
         -938339820,
         ThemeModule.LabelColor.AUTO,
         ThemeModule.CornerStyle.BEVEL,
         60,
         Map.of("specular", 18.0, "dispersion", 0.0, "rim", 14.0)
      ),
      MOSS(
         ThemeModule.Palette.NEON,
         ThemeModule.Preset.DARK,
         -1475669491,
         ThemeModule.LabelColor.AUTO,
         ThemeModule.CornerStyle.SQUIRCLE,
         90,
         Map.of("saturation", 110.0)
      );

      private final ThemeModule.Palette palette;
      private final ThemeModule.Preset glass;
      private final int tint;
      private final ThemeModule.LabelColor label;
      private final ThemeModule.CornerStyle corners;
      private final int cornerScale;
      private final Map<String, Double> numbers;

      Builtin(
         ThemeModule.Palette palette,
         ThemeModule.Preset glass,
         int tint,
         ThemeModule.LabelColor label,
         ThemeModule.CornerStyle corners,
         int cornerScale,
         Map<String, Double> numbers
      ) {
         this.palette = palette;
         this.glass = glass;
         this.tint = tint;
         this.label = label;
         this.corners = corners;
         this.cornerScale = cornerScale;
         this.numbers = numbers;
      }

      public ThemeModule.Palette palette() {
         return this.palette;
      }

      public ThemeModule.Preset glass() {
         return this.glass;
      }

      public int tint() {
         return this.tint;
      }

      public ThemeModule.LabelColor label() {
         return this.label;
      }

      public ThemeModule.CornerStyle corners() {
         return this.corners;
      }

      public int cornerScale() {
         return this.cornerScale;
      }

      public Map<String, Double> numbers() {
         return this.numbers;
      }
   }
}
