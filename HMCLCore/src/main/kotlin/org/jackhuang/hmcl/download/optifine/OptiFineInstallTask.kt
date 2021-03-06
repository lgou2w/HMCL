/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2017  huangyuhui <huanghongxun2008@126.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.download.optifine

import org.jackhuang.hmcl.download.DefaultDependencyManager
import org.jackhuang.hmcl.download.RemoteVersion
import org.jackhuang.hmcl.download.game.GameLibrariesTask
import org.jackhuang.hmcl.game.LibrariesDownloadInfo
import org.jackhuang.hmcl.game.Library
import org.jackhuang.hmcl.game.LibraryDownloadInfo
import org.jackhuang.hmcl.game.Version
import org.jackhuang.hmcl.task.Task
import org.jackhuang.hmcl.task.TaskResult
import org.jackhuang.hmcl.task.then
import org.jackhuang.hmcl.util.merge

/**
 * **Note**: OptiFine should be installed in the end.
 */
class OptiFineInstallTask(private val dependencyManager: DefaultDependencyManager,
                          private val gameVersion: String,
                          private val version: Version,
                          private val remoteVersion: String): TaskResult<Version>() {
    private val optiFineVersionList = dependencyManager.getVersionList("optifine")
    lateinit var remote: RemoteVersion<*>
    override val dependents = mutableListOf<Task>()
    override val dependencies = mutableListOf<Task>()
    override val id = "version"

    override val reliesOnDependencies = false

    init {
        if (!optiFineVersionList.loaded)
            dependents += optiFineVersionList.refreshAsync(dependencyManager.downloadProvider) then {
                remote = optiFineVersionList.getVersion(gameVersion, remoteVersion) ?: throw IllegalArgumentException("Remote OptiFine version $gameVersion-$remoteVersion not found")
                null
            }
        else {
            remote = optiFineVersionList.getVersion(gameVersion, remoteVersion) ?: throw IllegalArgumentException("Remote OptiFine version $gameVersion-$remoteVersion not found")
        }
    }

    override fun execute() {
        val library = Library(
                groupId = "net.optifine",
                artifactId = "optifine",
                version = remoteVersion,
                lateload = true,
                downloads = LibrariesDownloadInfo(
                        artifact = LibraryDownloadInfo(
                                path = "net/optifine/optifine/$remoteVersion/optifine-$remoteVersion.jar",
                                url = remote.url
                        )
                ))
        val libraries = mutableListOf(library)
        var arg = version.minecraftArguments!!
        if (!arg.contains("FMLTweaker"))
            arg += " --tweakClass optifine.OptiFineTweaker"
        var mainClass = version.mainClass
        if (mainClass == null || !mainClass.startsWith("net.minecraft.launchwrapper.")) {
            mainClass = "net.minecraft.launchwrapper.Launch"
            libraries.add(0, Library(
                    groupId = "net.minecraft",
                    artifactId = "launchwrapper",
                    version = "1.12"
            ))
        }
        result = version.copy(libraries = merge(version.libraries, libraries), mainClass = mainClass, minecraftArguments = arg)
        dependencies += GameLibrariesTask(dependencyManager, version.copy(libraries = libraries))
    }
}