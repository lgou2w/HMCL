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
package org.jackhuang.hmcl.ui

import com.jfoenix.controls.*
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.Toggle
import javafx.scene.control.ToggleGroup
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.jackhuang.hmcl.i18n
import org.jackhuang.hmcl.setting.EnumGameDirectory
import org.jackhuang.hmcl.setting.Profile
import org.jackhuang.hmcl.setting.VersionSetting
import org.jackhuang.hmcl.task.Scheduler
import org.jackhuang.hmcl.task.task
import org.jackhuang.hmcl.ui.construct.ComponentList
import org.jackhuang.hmcl.ui.construct.MultiFileItem
import org.jackhuang.hmcl.ui.construct.NumberValidator
import org.jackhuang.hmcl.util.JavaVersion
import org.jackhuang.hmcl.util.OS

class VersionSettingsController {
    var lastVersionSetting: VersionSetting? = null
    @FXML lateinit var rootPane: VBox
    @FXML lateinit var scroll: ScrollPane
    @FXML lateinit var txtWidth: JFXTextField
    @FXML lateinit var txtHeight: JFXTextField
    @FXML lateinit var txtMaxMemory: JFXTextField
    @FXML lateinit var txtJVMArgs: JFXTextField
    @FXML lateinit var txtGameArgs: JFXTextField
    @FXML lateinit var txtMetaspace: JFXTextField
    @FXML lateinit var txtWrapper: JFXTextField
    @FXML lateinit var txtPrecallingCommand: JFXTextField
    @FXML lateinit var txtServerIP: JFXTextField
    @FXML lateinit var advancedSettingsPane: ComponentList
    @FXML lateinit var cboLauncherVisibility: JFXComboBox<*>
    @FXML lateinit var chkFullscreen: JFXCheckBox
    @FXML lateinit var lblPhysicalMemory: Label
    @FXML lateinit var chkNoJVMArgs: JFXToggleButton
    @FXML lateinit var chkNoCommon: JFXToggleButton
    @FXML lateinit var chkNoGameCheck: JFXToggleButton
    @FXML lateinit var javaItem: MultiFileItem
    @FXML lateinit var gameDirItem: MultiFileItem
    @FXML lateinit var chkShowLogs: JFXToggleButton
    @FXML lateinit var btnIconSelection: JFXButton
    @FXML lateinit var iconView: ImageView

    lateinit var profile: Profile
    lateinit var versionId: String

    fun initialize() {
        lblPhysicalMemory.text = i18n("settings.physical_memory") + ": ${OS.TOTAL_MEMORY}MB"

        scroll.smoothScrolling()

        val limit = 300.0
        //txtJavaDir.limitWidth(limit)
        txtMaxMemory.limitWidth(limit)
        cboLauncherVisibility.limitWidth(limit)

        val limitHeight = 10.0
        chkNoJVMArgs.limitHeight(limitHeight)
        chkNoCommon.limitHeight(limitHeight)
        chkNoGameCheck.limitHeight(limitHeight)
        chkShowLogs.limitHeight(limitHeight)

        fun validator(nullable: Boolean = false) = NumberValidator(nullable).apply { message = "Must be a number." }

        txtWidth.setValidators(validator())
        txtWidth.setValidateWhileTextChanged()
        txtHeight.setValidators(validator())
        txtHeight.setValidateWhileTextChanged()
        txtMaxMemory.setValidators(validator())
        txtMaxMemory.setValidateWhileTextChanged()
        txtMetaspace.setValidators(validator(true))
        txtMetaspace.setValidateWhileTextChanged()

        task {
            it["list"] = JavaVersion.getJREs().values.map { javaVersion ->
                javaItem.createChildren(javaVersion.longVersion, javaVersion.binary.absolutePath, javaVersion)
            }
        }.subscribe(Scheduler.JAVAFX) {
            javaItem.loadChildren(it.get<Collection<Node>>("list"))
        }

        gameDirItem.loadChildren(listOf(
                gameDirItem.createChildren(i18n("advancedsettings.game_dir.default"), userData = EnumGameDirectory.ROOT_FOLDER),
                gameDirItem.createChildren(i18n("advancedsettings.game_dir.independent"), userData = EnumGameDirectory.VERSION_FOLDER)
        ))
    }

    fun loadVersionSetting(profile: Profile, versionId: String, version: VersionSetting) {
        rootPane.children -= advancedSettingsPane

        this.profile = profile
        this.versionId = versionId

        lastVersionSetting?.apply {
            widthProperty.unbind()
            heightProperty.unbind()
            maxMemoryProperty.unbind()
            javaArgsProperty.unbind()
            minecraftArgsProperty.unbind()
            permSizeProperty.unbind()
            wrapperProperty.unbind()
            precalledCommandProperty.unbind()
            serverIpProperty.unbind()
            fullscreenProperty.unbind()
            notCheckGameProperty.unbind()
            noCommonProperty.unbind()
            javaDirProperty.unbind()
            showLogsProperty.unbind()
            unbindEnum(cboLauncherVisibility)
        }

        bindInt(txtWidth, version.widthProperty)
        bindInt(txtHeight, version.heightProperty)
        bindInt(txtMaxMemory, version.maxMemoryProperty)
        bindString(javaItem.txtCustom, version.javaDirProperty)
        bindString(gameDirItem.txtCustom, version.gameDirProperty)
        bindString(txtJVMArgs, version.javaArgsProperty)
        bindString(txtGameArgs, version.minecraftArgsProperty)
        bindString(txtMetaspace, version.permSizeProperty)
        bindString(txtWrapper, version.wrapperProperty)
        bindString(txtPrecallingCommand, version.precalledCommandProperty)
        bindString(txtServerIP, version.serverIpProperty)
        bindEnum(cboLauncherVisibility, version.launcherVisibilityProperty)
        bindBoolean(chkFullscreen, version.fullscreenProperty)
        bindBoolean(chkNoGameCheck, version.notCheckGameProperty)
        bindBoolean(chkNoCommon, version.noCommonProperty)
        bindBoolean(chkShowLogs, version.showLogsProperty)

        val javaGroupKey = "java_group.listener"
        @Suppress("UNCHECKED_CAST")
        (javaItem.group.properties[javaGroupKey] as? ChangeListener<in Toggle>?)
                ?.run(javaItem.group.selectedToggleProperty()::removeListener)

        var flag = false
        var defaultToggle: JFXRadioButton? = null
        javaItem.group.toggles.filter { it is JFXRadioButton }.forEach { toggle ->
            if (toggle.userData == version.javaVersion) {
                toggle.isSelected = true
                flag = true
            } else if (toggle.userData == JavaVersion.fromCurrentEnvironment()) {
                defaultToggle = toggle as JFXRadioButton
            }
        }

        val listener = ChangeListener<Toggle> { _, _, newValue ->
            if (newValue == javaItem.radioCustom) { // Custom
                version.java = "Custom"
            } else {
                version.java = ((newValue as JFXRadioButton).userData as JavaVersion).longVersion
            }
        }
        javaItem.group.properties[javaGroupKey] = listener
        javaItem.group.selectedToggleProperty().addListener(listener)

        if (!flag) {
            defaultToggle?.isSelected = true
        }

        version.javaDirProperty.setChangedListener { initJavaSubtitle(version) }
        version.javaProperty.setChangedListener { initJavaSubtitle(version) }
        initJavaSubtitle(version)

        val gameDirKey = "game_dir.listener"
        @Suppress("UNCHECKED_CAST")
        (gameDirItem.group.properties[gameDirKey] as? ChangeListener<in Toggle>?)
                ?.run(gameDirItem.group.selectedToggleProperty()::removeListener)

        gameDirItem.group.toggles.filter { it is JFXRadioButton }.forEach { toggle ->
            if (toggle.userData == version.gameDirType) {
                toggle.isSelected = true
                flag = true
            }
        }

        gameDirItem.radioCustom.userData = EnumGameDirectory.CUSTOM

        val gameDirListener = ChangeListener<Toggle> { _, _, newValue ->
            version.gameDirType = (newValue as JFXRadioButton).userData as EnumGameDirectory
        }
        gameDirItem.group.properties[gameDirKey] = gameDirListener
        gameDirItem.group.selectedToggleProperty().addListener(gameDirListener)

        version.gameDirProperty.setChangedListener { initGameDirSubtitle(version) }
        version.gameDirTypeProperty.setChangedListener { initGameDirSubtitle(version) }
        initGameDirSubtitle(version)

        lastVersionSetting = version

        loadIcon()
    }

    private fun initJavaSubtitle(version: VersionSetting) {
        task { it["java"] = version.javaVersion }
                .subscribe(task(Scheduler.JAVAFX) { javaItem.subtitle = it.get<JavaVersion?>("java")?.binary?.absolutePath ?: "Invalid Java Directory" })
    }

    private fun initGameDirSubtitle(version: VersionSetting) {
        gameDirItem.subtitle = profile.repository.getRunDirectory(versionId).absolutePath
    }

    fun onShowAdvanced() {
        if (!rootPane.children.contains(advancedSettingsPane))
            rootPane.children += advancedSettingsPane
        else
            rootPane.children.remove(advancedSettingsPane)
    }

    fun onExploreIcon() {
        val chooser = FileChooser()
        chooser.extensionFilters += FileChooser.ExtensionFilter("Image", "*.png")
        val selectedFile = chooser.showOpenDialog(Controllers.stage)
        if (selectedFile != null) {
            val iconFile = profile.repository.getVersionIcon(versionId)
            selectedFile.copyTo(iconFile, overwrite = true)
            loadIcon()
        }
    }

    private fun loadIcon() {
        val iconFile = profile.repository.getVersionIcon(versionId)
        if (iconFile.exists())
            iconView.image = Image("file:" + iconFile.absolutePath)
        else
            iconView.image = DEFAULT_ICON
        iconView.limitSize(32.0, 32.0)
    }
}