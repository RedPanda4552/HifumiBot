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

import io.github.redpanda4552.HifumiBot.command.CommandMeta;
import io.github.redpanda4552.HifumiBot.permissions.PermissionLevel;

public abstract class AbstractCommand
{
    protected static final String CATEGORY_BUILTIN = "builtin", CATEGORY_NONE = "none";

    protected String name, category;
    protected PermissionLevel permissionLevel;
    protected boolean restrictChannel;

    public AbstractCommand(String name, String category, PermissionLevel permissionLevel, boolean restrictChannel)
    {
        this.name = name;
        this.category = category != null ? category : CATEGORY_NONE;
        this.permissionLevel = permissionLevel;
        this.restrictChannel = restrictChannel;
    }

    /**
     * Command payload.
     */
    public abstract void execute(CommandMeta cm);

    protected boolean isArgSingleWord(String arg)
    {
        return !arg.contains(" ");
    }

    public PermissionLevel getPermissionLevel()
    {
        return permissionLevel;
    }

    public String getName()
    {
        return name;
    }

    public String getCategory()
    {
        return category;
    }

    public boolean isRestricted()
    {
        return restrictChannel;
    }

    public abstract String getHelpText();
}
