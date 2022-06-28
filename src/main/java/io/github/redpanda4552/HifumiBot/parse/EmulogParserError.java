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
package io.github.redpanda4552.HifumiBot.parse;

public enum EmulogParserError {

    TLB_MISS( "(X) TLB miss. Usually caused by bad cheats, widescreen patches or disc dumps.\nGames can sometimes still function normally but usually just crash." ),
    TRAP_EXCEPTION( "(X) Trap exception. Usually thrown to signal the game has died. Though very\nunlikely, PCSX2 can on rare occasions still recover and continue." ),
    UNKNOWN_VIF1( "(X) Unknown VIF1 command. Usually a symptom caused by another severe problem, and a sign that emulation is about to fail." ),
    BLOCK_INDEX_EOF( "(X) The game tried to read past the end of its own disc contents. Usually indicates a disc image is damaged or incomplete." ),
    GS_MEM_FAIL(" (X) PCSX2 could not allocate memory for the PS2 GS. Usually causes graphics to stop working, or in some cases the game may stall." ),
    OUT_OF_VRAM( "(!) PCSX2 is out of or very low on VRAM." ),
    IOP_UNKNOWN_WRITE( "(!) Unknown write by IOP." ),
    VU0_TRIPLE_BRANCH( "(!) Triple branch detected in VU0." ),
    VU1_TRIPLE_BRANCH( "(!) Triple branch detected in VU1." ),
    SSTATE_FAIL( "(!) Savestate load failed." ),
    CPU_AC_POWER( "(!) CPU AC power states are not 100%" ),
    BIOS_FOUND( "(*) A PS2 BIOS ROM was found." ),
    AUTO_EJECT( "(*) Memory card auto-ejected." ),
    AUTO_EJECT_INSERT( "(*) Memory card inserted after auto-eject." ),
    SSTATE_LOAD( "(*) Loaded a savestate." ),
    SSTATE_SAVE( "(*) Saved a savestate." ),
    GAMEDB_GAMEFIX_LOADED( "(*) Loaded a gamefix from gamedb." ),
    GAMEDB_PATCH_LOADED( "(*) Loaded a patch from gamedb." ),
    WIDESCREEN_PATCH_LOADED( "(*) Loaded widescreen patches from a file." ),
    WIDESCREEN_PATCH_EMPTY( "(*) Found widescreen patch file, but nothing to load." ),
    WIDESCREEN_ARCHIVE_LOADED( "(*) Loaded widescreen patches from the archive." ),
    CHEAT_LOADED( "(*) Loaded cheats from a file." ),
    CHEAT_EMPTY( "(*) Found cheat file, but nothing to load." ),
    PATCH_ERROR( "(!) Error while parsing a line out of a cheat file" );

    private String displayString;

    private EmulogParserError(String displayString) {
        this.displayString = displayString;
    }

    public String getDisplayString() {
        return displayString;
    }
}
