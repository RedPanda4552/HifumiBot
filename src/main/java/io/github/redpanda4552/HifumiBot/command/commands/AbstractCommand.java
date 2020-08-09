/**
 * This file is part of HifumiBot, licensed under the MIT License (MIT)
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
package io.github.redpanda4552.HifumiBot.command.commands;

import io.github.redpanda4552.HifumiBot.HifumiBot;
import io.github.redpanda4552.HifumiBot.command.CommandMeta;

public abstract class AbstractCommand {

    protected static final String CATEGORY_BUILTIN = "builtin", CATEGORY_NONE = "none";
    
    protected String name, category;
    protected boolean admin;
    
    public AbstractCommand(String name, String category, boolean admin) {
        this.name = name;
        this.category = category != null ? category : CATEGORY_NONE;
        this.admin = admin;
    }
    
    /**
     * Do a prelimiary permissions check, and execute if it passes.
     */
    public void run(CommandMeta cm) {
        if (!isAdminCommand() || HifumiBot.getSelf().getPermissionManager().hasPermission(cm.getMember(), cm.getUser()))
            onExecute(cm);
    }
    
    /**
     * Command payload.
     */
    protected abstract void onExecute(CommandMeta cm);
    
    protected boolean isArgSingleWord(String arg) {
        return !arg.contains(" ");
    }
    
    public boolean isAdminCommand() {
        return admin;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public abstract String getHelpText();
}
