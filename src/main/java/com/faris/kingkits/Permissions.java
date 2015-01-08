package com.faris.kingkits;

import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

public class Permissions {
    public List<Permission> permissionsList = new ArrayList<Permission>();

    public Permission kitUseCommand = this.registerPermission(new Permission("kingkits.kit.use"));
    public Permission kitUseOtherCommand = this.registerPermission(new Permission("kingkits.kit.use.other"));
    public Permission kitCreateCommand = this.registerPermission(new Permission("kingkits.kit.create"));
    public Permission kitDeleteCommand = this.registerPermission(new Permission("kingkits.kit.delete"));
    public Permission kitRenameCommand = this.registerPermission(new Permission("kingkits.kit.rename"));
    public Permission kitUUseCommand = this.registerPermission(new Permission("kingkits.kit.uuse"));
    public Permission kitUCreateCommand = this.registerPermission(new Permission("kingkits.kit.ucreate"));
    public Permission kitUDeleteCommand = this.registerPermission(new Permission("kingkits.kit.udelete"));
    public Permission kitURenameCommand = this.registerPermission(new Permission("kingkits.kit.urename"));
    public Permission kitList = this.registerPermission(new Permission("kingkits.kit.list"));
    public Permission kitUList = this.registerPermission(new Permission("kingkits.kit.ulist"));
    public Permission kitBypassCooldown = this.registerPermission(new Permission("kingkits.kit.cooldown.bypass"));
    public Permission kitUseSign = this.registerPermission(new Permission("kingkits.kit.sign.use"));
    public Permission kitCreateSign = this.registerPermission(new Permission("kingkits.kit.sign.create"));
    public Permission kitListSign = this.registerPermission(new Permission("kingkits.kit.sign.list"));
    public Permission rightClickCompass = this.registerPermission(new Permission("kingkits.compass"));
    public Permission quickSoup = this.registerPermission(new Permission("kingkits.quicksoup"));
    public Permission refillSoupSingle = this.registerPermission(new Permission("kingkits.refill.single"));
    public Permission refillSoupAll = this.registerPermission(new Permission("kingkits.refill.all"));
    public Permission cmdConfigManagement = this.registerPermission(new Permission("kingkits.command.config"));
    public Permission killstreak = this.registerPermission(new Permission("kingkits.command.killstreak"));
    public Permission cmdReloadConfig = this.registerPermission(new Permission("kingkits.command.reload"));
    public Permission cmdSetCooldown = this.registerPermission(new Permission("kingkits.command.setcooldown"));

    private Permission registerPermission(Permission permission) {
        if (this.permissionsList == null) this.permissionsList = new ArrayList<Permission>();
        if (!this.permissionsList.contains(permission)) this.permissionsList.add(permission);
        return permission;
    }
}
