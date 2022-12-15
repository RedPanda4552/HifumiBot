// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot;

import io.github.redpanda4552.HifumiBot.util.Messaging;
import io.github.redpanda4552.HifumiBot.util.Refreshable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.Request;
import okhttp3.Response;
import org.yaml.snakeyaml.Yaml;

@SuppressWarnings("serial")
public class GameDB implements Refreshable {

  private static final String GAMEDB_LOCATION =
      "https://raw.githubusercontent.com/PCSX2/pcsx2/master/bin/resources/GameIndex.yaml";

  private static final HashMap<Integer, String> COMPAT =
      new HashMap<Integer, String>() {
        {
          put(0, "Unknown");
          put(1, "Nothing");
          put(2, "Intro");
          put(3, "Menu");
          put(4, "In-game");
          put(5, "Playable");
          put(6, "Perfect");
        }
      };

  private static final HashMap<Integer, String> ROUNDING =
      new HashMap<Integer, String>() {
        {
          put(0, "Nearest");
          put(1, "Negative Infinity");
          put(2, "Positive Infinity");
          put(3, "Chop (Zero)");
        }
      };

  private static final HashMap<Integer, String> EE_CLAMPING =
      new HashMap<Integer, String>() {
        {
          put(0, "Disable");
          put(1, "Normal");
          put(2, "Extra + Preserve Sign");
          put(3, "Full Clamping");
        }
      };

  private static final HashMap<Integer, String> VU_CLAMPING =
      new HashMap<Integer, String>() {
        {
          put(0, "Disable");
          put(1, "Normal");
          put(2, "Extra");
          put(3, "Extra + Preserve Sign");
        }
      };

  private static final HashMap<Integer, String> MIPMAP =
      new HashMap<Integer, String>() {
        {
          put(0, "Off");
          put(1, "Basic");
          put(2, "Full");
        }
      };

  private static final HashMap<Integer, String> TRILINEAR =
      new HashMap<Integer, String>() {
        {
          put(0, "None");
          put(1, "Trilinear");
          put(2, "Trilinear Ultra");
        }
      };

  private static final HashMap<Integer, String> TEXTURE_PRELOADING =
      new HashMap<Integer, String>() {
        {
          put(0, "None");
          put(1, "Partial");
          put(2, "Full (Hash Cache)");
        }
      };

  private static final HashMap<Integer, String> DEINTERLACING =
      new HashMap<Integer, String>() {
        {
          put(0, "Off");
          put(1, "Weave (TFF)");
          put(2, "Weave (BFF)");
          put(3, "Bob (TFF)");
          put(4, "Bob (BFF)");
          put(5, "Blend (TFF)");
          put(6, "Blend (BFF)");
          put(7, "Automatic");
        }
      };

  private static final HashMap<Integer, String> GENERIC_BOOLEAN =
      new HashMap<Integer, String>() {
        {
          put(0, "Off");
          put(1, "On");
        }
      };

  private static final HashMap<Integer, String> HALF_PIXEL_OFFSET =
      new HashMap<Integer, String>() {
        {
          put(0, "Off");
          put(1, "Normal (Vertex)");
          put(2, "Special (Texture)");
          put(3, "Special (Texture - Aggressive)");
        }
      };

  private static final HashMap<Integer, String> ROUND_SPRITE =
      new HashMap<Integer, String>() {
        {
          put(0, "Off");
          put(1, "Half");
          put(2, "Full");
        }
      };

  private static final HashMap<Integer, String> HALF_BOTTOM =
      new HashMap<Integer, String>() {
        {
          put(0, "Force Disabled");
          put(1, "Force Enabled");
        }
      };

  private Map<String, Object> map;

  public GameDB() {
    refresh();
  }

  @Override
  public void refresh() {
    Request req = new Request.Builder().url(GAMEDB_LOCATION).get().build();

    try {
      Response res = HifumiBot.getSelf().getHttpClient().newCall(req).execute();

      if (res.isSuccessful()) {
        Yaml yaml = new Yaml();
        map = yaml.load(res.body().charStream());
      }
    } catch (IOException e) {
      Messaging.logException("GameDB", "refresh", e);
    }
  }

  public Map<String, Object> getMap() {
    return map;
  }

  @SuppressWarnings("unchecked")
  public MessageEmbed present(String serial) {
    EmbedBuilder eb = new EmbedBuilder();
    boolean entryFound = false;

    for (String key : map.keySet()) {
      if (key.equals(serial.toUpperCase())) {
        entryFound = true;
        Map<String, Object> entry = (Map<String, Object>) map.get(key);

        eb.setTitle((String) entry.get("name"));

        if (entry.containsKey("region")) {
          eb.appendDescription("Region: " + (String) entry.get("region") + "\n");
        }

        if (entry.containsKey("compat")) {
          eb.appendDescription("Compatibility: " + COMPAT.get((int) entry.get("compat")));
        }

        if (entry.containsKey("roundModes")) {
          Map<String, Object> roundModes = (Map<String, Object>) entry.get("roundModes");

          if (roundModes.containsKey("eeRoundMode")) {
            eb.addField(
                "EE Rounding Mode", ROUNDING.get((int) roundModes.get("eeRoundMode")), true);
          }

          if (roundModes.containsKey("vuRoundMode")) {
            eb.addField(
                "VU Rounding Mode", ROUNDING.get((int) roundModes.get("vuRoundMode")), true);
          }
        }

        if (entry.containsKey("clampModes")) {
          Map<String, Object> clampModes = (Map<String, Object>) entry.get("clampModes");

          if (clampModes.containsKey("eeClampMode")) {
            eb.addField(
                "EE Clamping Mode", EE_CLAMPING.get((int) clampModes.get("eeClampMode")), true);
          }

          if (clampModes.containsKey("vuClampMode")) {
            eb.addField(
                "VU Clamping Mode", VU_CLAMPING.get((int) clampModes.get("vuClampMode")), true);
          }
        }

        if (entry.containsKey("gsHWFixes")) {
          Map<String, Object> gsHWFixes = (Map<String, Object>) entry.get("gsHWFixes");

          if (gsHWFixes.containsKey("mipmap")) {
            eb.addField("Mipmapping", MIPMAP.get((int) gsHWFixes.get("mipmap")), true);
          }

          if (gsHWFixes.containsKey("trilinearFiltering")) {
            eb.addField(
                "Trilinear Filtering",
                TRILINEAR.get((int) gsHWFixes.get("trilinearFiltering")),
                true);
          }

          if (gsHWFixes.containsKey("conservativeFramebuffer")) {
            eb.addField(
                "Conservative Framebuffer",
                GENERIC_BOOLEAN.get((int) gsHWFixes.get("conservativeFramebuffer")),
                true);
          }

          if (gsHWFixes.containsKey("texturePreloading")) {
            eb.addField(
                "Texture Preloading",
                TEXTURE_PRELOADING.get((int) gsHWFixes.get("texturePreloading")),
                true);
          }

          if (gsHWFixes.containsKey("deinterlace")) {
            eb.addField(
                "Deinterlacing", DEINTERLACING.get((int) gsHWFixes.get("deinterlace")), true);
          }

          if (gsHWFixes.containsKey("autoFlush")) {
            eb.addField("Auto Flush", GENERIC_BOOLEAN.get((int) gsHWFixes.get("autoFlush")), true);
          }

          if (gsHWFixes.containsKey("disableDepthSupport")) {
            eb.addField(
                "Disable Depth Emulation",
                GENERIC_BOOLEAN.get((int) gsHWFixes.get("disableDepthSupport")),
                true);
          }

          if (gsHWFixes.containsKey("disablePartialInvalidation")) {
            eb.addField(
                "Disable Partial Invalidation",
                GENERIC_BOOLEAN.get((int) gsHWFixes.get("disablePartialInvalidation")),
                true);
          }

          if (gsHWFixes.containsKey("cpuFramebufferConversion")) {
            eb.addField(
                "Frame Buffer Conversion",
                GENERIC_BOOLEAN.get((int) gsHWFixes.get("cpuFramebufferConversion")),
                true);
          }

          if (gsHWFixes.containsKey("wrapGSMem")) {
            eb.addField(
                "Memory Wrapping", GENERIC_BOOLEAN.get((int) gsHWFixes.get("wrapGSMem")), true);
          }

          if (gsHWFixes.containsKey("preloadFrameData")) {
            eb.addField(
                "Preload Frame Data",
                GENERIC_BOOLEAN.get((int) gsHWFixes.get("preloadFrameData")),
                true);
          }

          if (gsHWFixes.containsKey("textureInsideRT")) {
            eb.addField(
                "Texture Inside RT",
                GENERIC_BOOLEAN.get((int) gsHWFixes.get("textureInsideRT")),
                true);
          }

          if (gsHWFixes.containsKey("halfBottomOverride")) {
            eb.addField(
                "Half Screen Fix",
                HALF_BOTTOM.get((int) gsHWFixes.get("halfBottomOverride")),
                true);
          }

          if (gsHWFixes.containsKey("pointListPalette")) {
            eb.addField(
                "Disable Safe Features",
                GENERIC_BOOLEAN.get((int) gsHWFixes.get("pointListPalette")),
                true);
          }

          if (gsHWFixes.containsKey("alignSprite")) {
            eb.addField(
                "Align Sprite", GENERIC_BOOLEAN.get((int) gsHWFixes.get("alignSprite")), true);
          }

          if (gsHWFixes.containsKey("mergeSprite")) {
            eb.addField(
                "Merge Sprite", GENERIC_BOOLEAN.get((int) gsHWFixes.get("mergeSprite")), true);
          }

          if (gsHWFixes.containsKey("wildArmsHack")) {
            eb.addField(
                "Wild Arms Hack", GENERIC_BOOLEAN.get((int) gsHWFixes.get("wildArmsHack")), true);
          }

          if (gsHWFixes.containsKey("skipDrawStart")) {
            eb.addField(
                "Skipdraw Range (Start)",
                String.valueOf((int) gsHWFixes.get("skipDrawStart")),
                true);
          }

          if (gsHWFixes.containsKey("skipDrawEnd")) {
            eb.addField(
                "Skipdraw Range (End)", String.valueOf((int) gsHWFixes.get("skipDrawEnd")), true);
          }

          if (gsHWFixes.containsKey("halfPixelOffset")) {
            eb.addField(
                "Half Pixel Offset",
                HALF_PIXEL_OFFSET.get((int) gsHWFixes.get("halfPixelOffset")),
                true);
          }

          if (gsHWFixes.containsKey("roundSprite")) {
            eb.addField("Round Sprite", ROUND_SPRITE.get((int) gsHWFixes.get("roundSprite")), true);
          }
        }
      }
    }

    if (!entryFound) {
      eb.setTitle("No GameDB Entry Found");
      eb.setDescription(
          "Serial `"
              + serial
              + "` did not appear anywhere in GameDB. Verify spelling and try again.");
    }

    return eb.build();
  }
}
