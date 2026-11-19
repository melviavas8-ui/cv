package dev.cloudvisuals.render.gpu;

import dev.cloudvisuals.CloudVisualsClient;
import java.util.ArrayList;
import java.util.List;

public final class GpuFrame {
   private static final List<Runnable> listeners = new ArrayList<>();
   private static long frame;

   private GpuFrame() {
   }

   public static long frame() {
      return frame;
   }

   public static void onEnd(Runnable action) {
      listeners.add(action);
   }

   public static void end() {
      frame++;

      try {
         CloudVisualsShader.endFrame();
         GuiDraw.endFrame();

         for (Runnable listener : listeners) {
            listener.run();
         }
      } catch (RuntimeException e) {
         CloudVisualsClient.LOGGER.error("[Cloud Visuals] frame end failed", e);
      }
   }
}
