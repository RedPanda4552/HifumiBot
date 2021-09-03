/**
809 * This file is part of HifumiBot, licensed under the MIT License (MIT)
 * 
 * Copyright (c) 2020 RedPanda4552 (https://github.com/RedPanda4552)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.redpanda4552.HifumiBot.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.redpanda4552.HifumiBot.util.Messaging;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class EmulogParser extends AbstractParser {

    private final Message message;
    private Attachment attachment;

    private static Pattern iopUnknownWrite = Pattern.compile("IOP Unknown .+ write.*");
    private static Pattern acPower = Pattern.compile("AC: ([0-9]{1,3})% / ([0-9]{1,3})%");
    private static Pattern widescreenArchive = Pattern.compile("Loading patch '.{8,}.pnach' from archive.*");
    private static Pattern widescreenPatch = Pattern.compile("Loaded (\\d+) Widescreen hacks from '.{8,}.pnach' at.*");
    private static Pattern cheats = Pattern.compile("Loaded (\\d+) Cheats from '.{8,}.pnach' at.*");

    private HashMap<EmulogParserError, ArrayList<Integer>> errorMap;

    public EmulogParser(final Message message) {
        this.message = message;

        for (Attachment att : message.getAttachments()) {
            if (att.getFileName().equalsIgnoreCase("emulog.txt")) {
                this.attachment = att;
                break;
            }
        }

        this.errorMap = new HashMap<EmulogParserError, ArrayList<Integer>>();

        for (EmulogParserError ppe : EmulogParserError.values()) {
            this.errorMap.put(ppe, new ArrayList<Integer>());
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
            Messaging.sendMessage(message.getChannel(),
                    ":x: The URL to your attachment was bad... Try uploading again or changing the file name?");
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            Messaging.sendMessage(message.getChannel(), ":hourglass: " + message.getAuthor().getAsMention()
                    + " Checking your emulog.txt for information/errors...");
            int lineNumber = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                Matcher m = null;

                if (line.contains("TLB Miss")) {
                    addError(EmulogParserError.TLB_MISS, lineNumber);
                } else if (line.startsWith("Trap exception")) {
                    addError(EmulogParserError.TRAP_EXCEPTION, lineNumber);
                } else if (line.startsWith("[GameDB] Found patch with CRC")) {
                    addError(EmulogParserError.GAMEDB_PATCH_LOADED, lineNumber);
                } else if (line.startsWith("microVU0 Warning: Branch, Branch, Branch!")) {
                    addError(EmulogParserError.VU0_TRIPLE_BRANCH, lineNumber);
                } else if (line.startsWith("microVU1 Warning: Branch, Branch, Branch!")) {
                    addError(EmulogParserError.VU1_TRIPLE_BRANCH, lineNumber);
                } else if ((m = iopUnknownWrite.matcher(line)).matches()) {
                    addError(EmulogParserError.IOP_UNKNOWN_WRITE, lineNumber);
                } else if (line.startsWith("Loading savestate")) {
                    addError(EmulogParserError.SSTATE_LOAD, lineNumber);
                } else if (line.startsWith("Saving savestate")) {
                    addError(EmulogParserError.SSTATE_SAVE, lineNumber);
                } else if (line.startsWith("Savestate is corrupt or incomplete")) {
                    addError(EmulogParserError.SSTATE_FAIL, lineNumber);
                } else if (line.contains("Auto-ejecting memcard")) {
                    addError(EmulogParserError.AUTO_EJECT, lineNumber);
                } else if (line.contains("Re-inserting auto-ejected memcard")) {
                    addError(EmulogParserError.AUTO_EJECT_INSERT, lineNumber);
                } else if (line.startsWith("isoFile error: Block index is past the end of file!")) {
                    addError(EmulogParserError.BLOCK_INDEX_EOF, lineNumber);
                } else if (line.startsWith("Available VRAM is very low")) {
                    addError(EmulogParserError.OUT_OF_VRAM, lineNumber);
                } else if (line.startsWith("(GameDB) Enabled Gamefix:")) {
                    addError(EmulogParserError.GAMEDB_GAMEFIX_LOADED, lineNumber);
                } else if (line.trim().startsWith("Bios Found:")) {
                    addError(EmulogParserError.BIOS_FOUND, lineNumber);
                } else if ((m = acPower.matcher(line)).matches()) {
                    try {
                        int first = Integer.parseInt(m.group(1));
                        int second = Integer.parseInt(m.group(2));

                        if (first != 100 || second != 100) {
                            addError(EmulogParserError.CPU_AC_POWER, lineNumber);
                        }
                    } catch (NumberFormatException e) {
                    }
                } else if ((m = widescreenArchive.matcher(line)).matches()) {
                    addError(EmulogParserError.WIDESCREEN_ARCHIVE_LOADED, lineNumber);
                } else if ((m = widescreenPatch.matcher(line)).matches()) {
                    try {
                        int patches = Integer.parseInt(m.group(1));

                        if (patches > 0) {
                            addError(EmulogParserError.WIDESCREEN_PATCH_LOADED, lineNumber);
                        } else {
                            addError(EmulogParserError.WIDESCREEN_PATCH_EMPTY, lineNumber);
                        }
                    } catch (NumberFormatException e) {
                    }
                } else if ((m = cheats.matcher(line)).matches()) {
                    try {
                        int patches = Integer.parseInt(m.group(1));

                        if (patches > 0) {
                            addError(EmulogParserError.CHEAT_LOADED, lineNumber);
                        } else {
                            addError(EmulogParserError.CHEAT_EMPTY, lineNumber);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }

            reader.close();

            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("\n")
                    .append("============================= Emulog Parse Results =============================")
                    .append("\n");
            bodyBuilder.append("(*) = Information (!) = Warning (X) = Critical").append("\n\n");
            boolean hasLines = false;

            for (EmulogParserError epe : errorMap.keySet()) {
                ArrayList<Integer> lines = errorMap.get(epe);

                if (lines.size() > 0) {
                    hasLines = true;
                    bodyBuilder
                            .append("--------------------------------------------------------------------------------")
                            .append("\n");
                    bodyBuilder.append(epe.getDisplayString()).append("\n\n");
                    bodyBuilder.append("Affected Lines:").append("\n");
                    StringBuilder lineBuilder = new StringBuilder();

                    for (Integer i : lines) {
                        if (lineBuilder.length() + String.valueOf(i).length()
                                + String.valueOf(LINE_NUM_SEPARATOR).length() > MAX_LINE_LENGTH) {
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
                bodyBuilder.append("\n\n")
                        .append("=========================== End Emulog Parse Results ===========================")
                        .append("\n");

                Messaging.sendMessage(message.getChannel(),
                        ":information_source: Found something! Results are in this text file!",
                        "Emulog_" + message.getAuthor().getName() + ".txt", bodyBuilder.toString());
            } else {
                Messaging.sendMessage(message.getChannel(),
                        ":white_check_mark: Nothing to report! Either this emulog is empty, or things just went really well!");
            }
        } catch (IOException e) {
            Messaging.sendMessage(message.getChannel(), ":x: Something went wrong... Try again?");
            Messaging.logException("EmulogParser", "run", e);
            return;
        }
    }

    private void addError(EmulogParserError ppe, Integer line) {
        ArrayList<Integer> lines = errorMap.get(ppe);
        lines.add(line);
        errorMap.put(ppe, lines);
    }
}
