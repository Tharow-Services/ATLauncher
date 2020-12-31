/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.atlauncher.App;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.gui.dialogs.CurseModFileSelectorDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseApi;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class DisableableMod implements Serializable {
    public String name;
    public String version;
    public boolean optional;
    public String file;
    public Type type;
    public Color colour;
    public String description;
    public boolean disabled;
    public boolean userAdded = false; // Default to not being user added
    public boolean wasSelected = true; // Default to it being selected on install
    public Integer curseModId;
    public Integer curseFileId;
    public CurseMod curseMod;
    public CurseFile curseFile;

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, Integer curseModId,
            Integer curseFileId, CurseMod curseMod, CurseFile curseFile) {
        this.name = name;
        this.version = version;
        this.optional = optional;
        this.file = file;
        this.type = type;
        this.colour = colour;
        this.description = description;
        this.disabled = disabled;
        this.userAdded = userAdded;
        this.wasSelected = wasSelected;
        this.curseModId = curseModId;
        this.curseFileId = curseFileId;
        this.curseMod = curseMod;
        this.curseFile = curseFile;
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, CurseMod curseMod,
            CurseFile curseFile) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, wasSelected, curseMod.id,
                curseFile.id, curseMod, curseFile);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, Integer curseModId,
            Integer curseFileId) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, wasSelected, curseModId,
                curseFileId, null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, wasSelected, null, null,
                null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, true, null, null, null,
                null);
    }

    public DisableableMod() {
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public boolean hasColour() {
        return this.colour != null;
    }

    public Color getColour() {
        return this.colour;
    }

    public String getDescription() {
        if (this.description == null) {
            return "";
        }
        return this.description;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean wasSelected() {
        return this.wasSelected;
    }

    public void setWasSelected(boolean wasSelected) {
        this.wasSelected = wasSelected;
    }

    public boolean isUserAdded() {
        return this.userAdded;
    }

    public boolean isFromCurse() {
        return this.curseModId != null && this.curseFileId != null;
    }

    public boolean hasFullCurseInformation() {
        return this.curseMod != null && this.curseFile != null;
    }

    public Integer getCurseModId() {
        return this.curseModId;
    }

    public Integer getCurseFileId() {
        return this.curseFileId;
    }

    public String getFilename() {
        return this.file;
    }

    public boolean enable(Instance instance) {
        if (this.disabled) {
            if (!getFile(instance).getParentFile().exists()) {
                getFile(instance).getParentFile().mkdir();
            }
            if (Utils.moveFile(getDisabledFile(instance), getFile(instance), true)) {
                this.disabled = false;
            }
        }
        return false;
    }

    public boolean disable(Instance instance) {
        if (!this.disabled) {
            if (Utils.moveFile(getFile(instance), getDisabledFile(instance), true)) {
                this.disabled = true;
                return true;
            }
        }
        return false;
    }

    public boolean doesFileExist(Instance instance) {
        if (isDisabled()) {
            return getDisabledFile(instance).exists();
        }

        return getFile(instance).exists();
    }

    public File getDisabledFile(Instance instance) {
        return instance.getRoot().resolve("disabledmods/" + this.file).toFile();
    }

    public File getFile(Instance instance) {
        return getFile(instance.getRoot(), null);
    }

    public File getFile(Instance instance, Path base) {
        return getFile(base, null);
    }

    public File getFile(Path base) {
        return getFile(base, null);
    }

    public File getFile(Path base, String mcVersion) {
        File dir = null;
        switch (type) {
            case jar:
            case forge:
            case mcpc:
                dir = base.resolve("jarmods").toFile();
                break;
            case texturepack:
                dir = base.resolve("texturepacks").toFile();
                break;
            case resourcepack:
                dir = base.resolve("resourcepacks").toFile();
                break;
            case mods:
                dir = base.resolve("mods").toFile();
                break;
            case ic2lib:
                dir = base.resolve("mods/ic2").toFile();
                break;
            case denlib:
                dir = base.resolve("mods/denlib").toFile();
                break;
            case coremods:
                dir = base.resolve("coremods").toFile();
                break;
            case shaderpack:
                dir = base.resolve("shaderpacks").toFile();
                break;
            case dependency:
                if (mcVersion != null) {
                    dir = base.resolve("mods/" + mcVersion).toFile();
                }
                break;
            default:
                LogManager.warn("Unsupported mod for enabling/disabling " + this.name);
                break;
        }
        if (dir == null) {
            return null;
        }
        return new File(dir, file);
    }

    public Type getType() {
        return this.type;
    }

    public boolean checkForUpdate(Instance instance) {
        Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "UpdateMods", "Instance");
        List<CurseFile> curseModFiles = CurseApi.getFilesForMod(curseModId);

        Stream<CurseFile> curseFilesStream = curseModFiles.stream()
                .sorted(Comparator.comparingInt((CurseFile file) -> file.id).reversed());

        if (!App.settings.disableAddModRestrictions) {
            curseFilesStream = curseFilesStream
                    .filter(file -> App.settings.disableAddModRestrictions || file.gameVersion.contains(instance.id));
        }

        if (curseFilesStream.noneMatch(mod -> mod.id > curseFileId)) {
            return false;
        }

        new CurseModFileSelectorDialog(CurseApi.getModById(curseModId), instance, curseFileId);

        return true;
    }

    public boolean reinstall(Instance instance) {
        Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "ReinstallMods", "Instance");

        new CurseModFileSelectorDialog(CurseApi.getModById(curseModId), instance, curseFileId);

        return true;
    }
}
