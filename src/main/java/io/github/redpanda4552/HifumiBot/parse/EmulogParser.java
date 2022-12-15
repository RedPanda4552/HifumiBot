// SPDX-License-Identifier: MIT
package io.github.redpanda4552.HifumiBot.parse;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.config.EmulogParserConfig.Rule;
import io.github.redpanda4552.HifumiBot.util.Messaging;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class EmulogParser extends AbstractParser {

  private final Message message;
  private Attachment attachment;

  private final HashMap<Rule, Pattern> patterns;
  private final HashMap<Rule, ArrayList<Integer>> lines;

  public EmulogParser(final Message message) {
    this.message = message;

    for (Attachment att : message.getAttachments()) {
      if (att.getFileName().equalsIgnoreCase("emulog.txt")) {
        this.attachment = att;
        break;
      }
    }

    patterns = new HashMap<>();
    lines = new HashMap<>();

    for (Rule rule : HifumiBot.getSelf().getEmulogParserConfig().rules) {
      patterns.put(rule, Pattern.compile(rule.toMatch.toLowerCase()));
      lines.put(rule, new ArrayList<>());
    }
  }

  @Override
  public void run() {
    if (attachment == null) {
      return;
    }

    URL url = null;

    try {
      url = new URL(attachment.getUrl());
    } catch (MalformedURLException e) {
      Messaging.sendMessage(
          message.getChannel(),
          ":x: The URL to your attachment was bad... Try uploading again or changing the file"
              + " name?");
      return;
    }

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
      Messaging.sendMessage(
          message.getChannel(),
          ":hourglass: "
              + message.getAuthor().getAsMention()
              + " Checking your emulog.txt for information/errors...");
      int lineNumber = 0;
      String line;

      while ((line = reader.readLine()) != null) {
        line = line.toLowerCase();

        lineNumber++;

        for (Rule rule : patterns.keySet()) {
          Pattern p = patterns.get(rule);
          Matcher m = p.matcher(line);

          if (m.matches()) {
            addError(rule, lineNumber);
          }
        }
      }

      reader.close();

      StringBuilder bodyBuilder = new StringBuilder();
      bodyBuilder
          .append("\n")
          .append(
              "============================= Emulog Parse Results =============================")
          .append("\n");
      bodyBuilder.append("(*) = Information (!) = Warning (X) = Critical").append("\n\n");
      boolean hasLines = false;

      for (Rule rule : lines.keySet()) {
        ArrayList<Integer> arr = lines.get(rule);

        if (arr.size() > 0) {
          hasLines = true;
          bodyBuilder
              .append(
                  "--------------------------------------------------------------------------------")
              .append("\n");

          switch (rule.severity) {
            case 0:
              bodyBuilder.append("(*) ");
              break;
            case 1:
              bodyBuilder.append("(!) ");
              break;
            case 2:
              bodyBuilder.append("(X) ");
              break;
            default:
              bodyBuilder.append("(?) ");
              break;
          }

          bodyBuilder.append(rule.message).append("\n\n");
          bodyBuilder.append("Affected Lines:").append("\n");
          StringBuilder lineBuilder = new StringBuilder();

          for (Integer i : arr) {
            if (lineBuilder.length()
                    + String.valueOf(i).length()
                    + String.valueOf(LINE_NUM_SEPARATOR).length()
                > MAX_LINE_LENGTH) {
              bodyBuilder.append(lineBuilder.toString()).append("\n");
              lineBuilder = new StringBuilder();
            } else if (lineBuilder.length() != 0) {
              lineBuilder.append(LINE_NUM_SEPARATOR);
            }

            lineBuilder.append(i);
          }

          if (lineBuilder.length() != 0) {
            bodyBuilder.append(lineBuilder.toString()).append("\n");
          }
        }
      }

      if (hasLines) {
        bodyBuilder
            .append("\n\n")
            .append(
                "=========================== End Emulog Parse Results ===========================")
            .append("\n");

        if (bodyBuilder.toString().getBytes().length
            <= HifumiBot.getSelf().getJda().getSelfUser().getAllowedFileSize()) {
          Messaging.sendMessage(
              message.getChannel(),
              ":information_source: Found something! Results are in this text file!",
              "Emulog_" + message.getAuthor().getName() + ".txt",
              bodyBuilder.toString());
        } else {
          Messaging.sendMessage(
              message.getChannel(),
              ":warning: Your emulog generated such a large results file that I can't upload it. A"
                  + " human is gonna have to read through your log manually.");
        }
      } else {
        Messaging.sendMessage(
            message.getChannel(),
            ":white_check_mark: Nothing to report! Either this emulog is empty, or things just went"
                + " really well!");
      }
    } catch (Exception e) {
      Messaging.sendMessage(message.getChannel(), ":x: Something went wrong... Try again?");
      Messaging.logException("EmulogParser", "run", e);
      return;
    }
  }

  private void addError(Rule rule, Integer line) {
    ArrayList<Integer> arr = lines.get(rule);
    arr.add(line);
    lines.put(rule, arr);
  }
}
