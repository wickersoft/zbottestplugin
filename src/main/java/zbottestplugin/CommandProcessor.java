/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zbottestplugin;

import zbottestplugin.oldshit.TaskFloor;
import zbottestplugin.oldshit.TranslationService;
import zbottestplugin.oldshit.TaskWalls;
import zbottestplugin.oldshit.TaskFish;
import edu.kit.informatik.AStar;
import edu.kit.informatik.GeometricPath;
import edu.kit.informatik.Node;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import zbottestplugin.HTTP.HTTPResponse;
import zedly.zbot.ClientSettings;
import zedly.zbot.EntityType;
import zedly.zbot.entity.Entity;
import zedly.zbot.entity.Player;
import zedly.zbot.Location;
import zedly.zbot.inventory.Enchantment;
import zedly.zbot.inventory.ItemStack;
import zedly.zbot.environment.Block;
import zedly.zbot.Material;
import zbottestplugin.enchantengine2.EnchantEngine;
import zbottestplugin.enchantengine2.LibraryLocation;
import zbottestplugin.enchantengine2.TaskBuyBookShelves;
import zbottestplugin.enchantengine2.TaskBuyLapis;
import zbottestplugin.enchantengine2.TaskLookUpOneSlot;
import zbottestplugin.enchantengine2.TaskPigCamper;
import zbottestplugin.enchantengine2.TaskRetrieveOneItem;
import zbottestplugin.enchantengine2.TaskScanLibrary;
import zbottestplugin.enchantengine2.TaskSellBooks;
import zbottestplugin.enchantengine2.TaskSellIron;
import zbottestplugin.enchantengine2.TaskStoreBooks;
import zedly.zbot.entity.FallingBlock;
import zedly.zbot.entity.Item;
import zedly.zbot.entity.LivingEntity;
import zedly.zbot.entity.Sheep;
import zedly.zbot.entity.Tameable;
import zedly.zbot.entity.Unknown;
import zedly.zbot.BlockFace;
import zedly.zbot.entity.Monster;
import zedly.zbot.entity.ZombieVillager;
import zedly.zbot.inventory.FurnaceInventory;
import zedly.zbot.inventory.Inventory;
import zedly.zbot.inventory.Trade;
import zedly.zbot.inventory.VillagerInventory;

/**
 *
 * @author Dennis
 */
public class CommandProcessor {

    public static void onCommand(String player, String command, boolean pm) throws Exception {
        String[] args = command.split(" ");
        String respondTo = null;
        if (pm) {
            respondTo = player;
        }
        switch (args[0]) {
            case "hi":
                respond(respondTo, "Hello " + player + "!");
                break;
            case "clock":
                respond(respondTo, "" + System.currentTimeMillis());
                break;
            case "forum":
                respond(respondTo, "&aOfficial Forum: &ehttp://nyanpig-forum.enjin.com/forum");
                break;
            case "fb":
                respond(respondTo, "&aNyanCat Server on Facebook: &ehttps://www.facebook.com/groups/406113456101737/");
                break;
            case "vote":
                respond(respondTo, "&aVote for us: &ehttp://www.nyancat.de/vote");
                break;
            case "discord":
                respond(respondTo, "&aOfficial Discord Server: &ehttps://discord.gg/4Qm3wrZ");
                break;
            case "stats":
                HTTPResponse http = HTTP.https("https://view.inews.qq.com/g2/getOnsInfo?name=disease_h5", "UTF-8");
                String s_json = new String(http.getContent());
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(s_json);
                Object o = json.get("data");
                if (!(o instanceof String)) {
                    System.err.println("Unable to get stats right now! (1)");
                    return;
                }
                String s_data = (String) o;
                JSONParser dataParser = new JSONParser();
                JSONObject dataJson = (JSONObject) dataParser.parse(s_data);
                o = dataJson.get("chinaTotal");
                if (!(o instanceof JSONObject)) {
                    System.err.println("Unable to get stats right now! (2)");
                    return;
                }
                JSONObject chinaTotal = (JSONObject) o;
                
                long confirmed = (long) (Long) chinaTotal.get("confirm");
                long suspected = (long) (Long) chinaTotal.get("suspect");
                long recovered = (long) (Long) chinaTotal.get("heal");
                long deceased = (long) (Long) chinaTotal.get("dead");
                respond(respondTo, "&cConfirmed: " + confirmed + " &eSuspected: " + suspected + " &aRecovered: " + recovered + " &7Deceased: " + deceased);
                break;
            case "google":
                if (args.length == 1) {
                    respond(respondTo, "google {query}");
                    return;
                }
                try {
                    int offset = 0;
                    http = HTTP.https("https://www.google.com/search?lr=lang_en&q=" + URLEncoder.encode(command.substring(7), "UTF-8"));
                    String html = new String(http.getContent());
                    String[] links = StringUtil.extractAll(html, "<div class=\"r\"><a href=\"", "\"");
                    if (links.length >= 1) {
                        System.out.println(links[0]);
                        //TODO: tinyurl
                        Storage.watcher.previewNextLink();
                        respond(respondTo, links[0]);
                        return;
                    }
                    respond(respondTo, "No results! :(");
                    return;
                } catch (IOException ex) {
                    respond(respondTo, "IO Error! o.O");
                    ex.printStackTrace();
                    return;
                }
            case "query":
                if (args.length == 1) {
                    respond(respondTo, "Shop for enchanted books! /msg " + Storage.self.getName() + " query fire_aspect-2 unbreaking-3");
                    break;
                }
                int results = EnchantEngine.queryEnchantment(command.substring(command.indexOf(" ") + 1));
                if (results == 0) {
                    respond(respondTo, "No results! Check your spelling or we might be out of stock");
                    break;
                }
                int absoluteIndex = EnchantEngine.getQueryResult(0);
                int estPrice = EnchantEngine.getPriceEstimate(absoluteIndex);
                String enchantString = EnchantEngine.getEnchantString(absoluteIndex);
                respond(respondTo, "$" + (estPrice / 100) + "." + (estPrice % 100) + ": " + enchantString + " (1/" + (results) + ": result <n>)");
                break;
            case "result":
                if (args.length != 2 || !args[1].matches("\\d+")) {
                    respond(respondTo, "result <n>: Browse search results");
                    break;
                }
                results = EnchantEngine.getNumResults();
                int resultIndex = Integer.parseInt(args[1]) - 1;
                if (EnchantEngine.getQueryResult(resultIndex) == -1) {
                    respond(respondTo, "Invalid result number! Use 1-" + EnchantEngine.getNumResults());
                } else {
                    absoluteIndex = EnchantEngine.getQueryResult(resultIndex);
                    String itemString = EnchantEngine.getEnchantString(absoluteIndex);
                    estPrice = EnchantEngine.getPriceEstimate(absoluteIndex);
                    respond(respondTo, "$" + (estPrice / 100) + "." + (estPrice % 100) + ": " + itemString + " (" + (resultIndex + 1) + "/" + (results) + ": result <n>)");
                }
                break;
        }
        if (!ZBotTestPlugin.admins.contains(player)) {
            return;
        }
        switch (args[0]) {
            case "trans":
            case "translate":
                if (args.length < 2) {
                    respond(respondTo, "translate [player] ([source language] ([your language]))");
                    return;
                }
                if (args.length == 2) {
                    if (args[1].equals("off")) {
                        TranslationService.disableAllTranslationsFor(player);
                        respond(respondTo, "Disabled all translations");
                        return;
                    }
                    TranslationService.removeTranslationFor(args[1], player);
                    respond(respondTo, "Translation of " + args[1] + " disabled");
                    return;
                }
                String sourceLanguage = "";
                String targetLanguage = "en";
                if (args.length >= 3) {
                    if (Storage.languageCodes.contains(args[2])) {
                        sourceLanguage = args[2];
                    } else if (Storage.languageAliases.containsKey(args[2])) {
                        sourceLanguage = Storage.languageAliases.get(args[2]);
                    } else {
                        respond(respondTo, "Unknown language!");
                        return;
                    }
                }
                if (args.length >= 4) {
                    if (Storage.languageCodes.contains(args[3])) {
                        targetLanguage = args[3];
                    } else if (Storage.languageAliases.containsKey(args[3])) {
                        targetLanguage = Storage.languageAliases.get(args[3]);
                    } else {
                        respond(respondTo, "Unknown language!");
                        return;
                    }
                }
                TranslationService.addTranslationFor(args[1], player, sourceLanguage + "|" + targetLanguage);
                respond(respondTo, "Translating " + args[1] + " from " + sourceLanguage + " to " + targetLanguage + "!");
                break;
            case "ad":
                String adItem = EnchantEngine.adItems[Storage.rnd.nextInt(EnchantEngine.adItems.length)];
                int results = EnchantEngine.queryEnchantment(adItem);
                if (results == 0) {
                    respond(respondTo, "No listings available for " + adItem);
                    break;
                }
                int resultNumber = EnchantEngine.getNumResults();
                int cheapestItem = EnchantEngine.getQueryResult(0);
                int price = EnchantEngine.getPriceEstimate(cheapestItem);
                Storage.self.sendChat("Get one of " + resultNumber + " " + adItem + " books starting at $" + (price / 100) + "." + (price % 100) + "! /msg " + Storage.self.getName() + " query " + adItem);
                break;
            case "entity":
                Entity ent = null;
                if (args.length == 2) {
                    ent = Storage.self.getEnvironment().getEntityById(Integer.parseInt(args[1]));
                } else {
                    double d = Double.MAX_VALUE;
                    double c;
                    Collection<Entity> ents = Storage.self.getEnvironment().getEntities();
                    System.out.println(ents.size() + " entities visible");
                    for (Entity e : ents) {
                        c = e.getLocation().distanceTo(Storage.self.getLocation());
                        System.out.println(e.getType() + " at " + e.getLocation());
                        if (c < d) {
                            d = c;
                            ent = e;
                        }
                    }
                }
                if (ent == null) {
                    Storage.self.sendChat("That entity is not visible :(");
                    return;
                }

                if (ent instanceof Sheep) {
                    Sheep s = (Sheep) ent;
                    Storage.self.sendChat("That is " + (s.isSheared() ? "sheared" : "") + s.getColor() + " SHEEP " + ent.getEntityId());
                } else if (ent instanceof Unknown) {
                    Storage.self.sendChat("That is unidentified entity " + +ent.getEntityId() + " (Type " + ((Unknown) ent).getEntityTypeId() + ")");
                } else if (ent instanceof Item) {
                    Item item = (Item) ent;
                    ItemStack is = item.getItemStack();
                    Storage.self.sendChat("That is ITEM " + ent.getEntityId() + " consisting of " + is.getAmount() + "x" + is.getType());
                } else if (ent instanceof Player) {
                    Storage.self.sendChat("That is PLAYER " + ent.getEntityId() + ", " + ((Player) ent).getName());
                } else if (ent instanceof FallingBlock) {
                    FallingBlock fb = (FallingBlock) ent;
                    Storage.self.sendChat("That is FALLING_BLOCK " + ent.getEntityId() + " consisting of " + fb.getBlockType());
                } else if (ent instanceof Tameable) {
                    Tameable t = (Tameable) ent;
                    Storage.self.sendChat("That " + ent.getType() + (t.hasOwner() ? (" belongs to " + t.getOwner()) : " has no owner"));
                } else {
                    Storage.self.sendChat("That is " + ent.getType() + " " + ent.getEntityId());
                }
                break;
            case "respawn":
                Storage.self.respawn();
                break;
            case "say":
                Storage.self.sendChat(command.substring(4));
                break;
            case "block":
                Block block;
                if (args.length >= 4) {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    Location loc = new Location(x, y, z).centerHorizontally();
                    block = Storage.self.getEnvironment().getBlockAt(loc);
                } else {
                    ent = Storage.self.getEnvironment().getEntityById(Storage.recorder.getEntityId());
                    block = Storage.self.getEnvironment().getBlockAt(ent.getLocation());
                }
                respond(respondTo, block.toString());
                if (block.hasTile()) {
                    respond(respondTo, "Tile: " + block.getTile());
                }
                break;
            case "rblock":
                if (args.length >= 4) {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);
                    block = Storage.self.getEnvironment().getBlockAt(Storage.self.getLocation()).getRelative(x, y, z);
                    respond(respondTo, block.toString());
                }
                break;
            case "debug":
                Storage.debugEntity = Integer.parseInt(args[1]);
                Storage.self.sendChat("Debugging " + Storage.debugEntity);
                break;
            case "skin":
                ClientSettings cs = Storage.self.getClientSettings();
                if (args.length == 2) {
                    switch (args[1]) {
                        case "normal":
                            cs.setSkinFlags(0x40);
                            break;
                        case "magic":
                            cs.setSkinFlags(0x7F);
                            break;
                    }
                }
                Storage.self.setClientSettings(cs);
                break;
            case "focus":
                if (Storage.recorder.isRunning()) {
                    respond(respondTo, "Cannot change focus while recording");
                    return;
                }
                int entityId = Integer.parseInt(args[1]);
                if (entityId == 0) {
                    Storage.recorder.setEntityId(Storage.self.getEntityId());
                    respond(respondTo, "Now focusing on myself");
                    return;
                }
                ent = Storage.self.getEnvironment().getEntityById(entityId);
                if (ent == null) {
                    respond(respondTo, "Unrecognized entity ID");
                } else if (ent instanceof Player) {
                    Storage.recorder.setEntityId(Integer.parseInt(args[1]));
                    respond(respondTo, "Now focusing on " + Storage.self.getEnvironment().getPlayerNameByUUID(((Player) ent).getUUID()));
                }
                break;
            case "goto":
                if (Storage.roamer.isRoaming()) {
                    respond(respondTo, "Stop the roaming process first");
                    return;
                }
                Location loc = null;

                if (args.length == 2 && args[1].equals("me")) {
                    UUID uuid = Storage.self.getEnvironment().getUUIDByPlayerName(player);
                    for (Entity e : Storage.self.getEnvironment().getEntities()) {
                        if (e instanceof Player && ((Player) e).getUUID().equals(uuid)) {
                            loc = e.getLocation();
                        }
                    }
                    if (loc == null) {
                        respond(respondTo, "I can't see you!");
                        return;
                    }
                } else if (args.length >= 4) {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    loc = new Location(x, y, z).centerHorizontally();
                } else {
                    ent = Storage.self.getEnvironment().getEntityById(Storage.recorder.getEntityId());
                    loc = ent.getLocation().centerHorizontally();
                }
                new TaskAINavigate(loc).start();
                break;
            case "sudo":
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length - 1; i++) {
                    sb.append(args[i]).append(" ");
                }
                sb.append(args[args.length - 1]);
                onCommand(args[1], sb.toString(), false);
                break;
            case "select":
                if (args.length == 2) {
                    int slot = Integer.parseInt(args[1]);
                    Storage.self.selectSlot(slot);
                }
                break;
            case "slot":
                if (args.length == 1) {
                    Class clazz = Storage.self.getInventory().getClass();
                    System.out.println(clazz);
                    ItemStack is = Storage.self.getInventory().getItemInHand();
                    respond(respondTo, is.getAmount() + "x " + is.getType());
                } else if (args.length == 2) {
                    ItemStack is = Storage.self.getInventory().getSlot(Integer.parseInt(args[1]));
                    if (is == null) {
                        respond(respondTo, "That slot is empty");
                    } else {
                        respond(respondTo, is.getAmount() + "x " + is.getType());
                    }
                }
                break;
            case "break":
                if (args.length == 4) {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);
                    Storage.self.breakBlock(x, y, z);
                } else if (args.length == 5) {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);
                    int ms = Integer.parseInt(args[4]);
                    Storage.self.breakBlock(x, y, z, ms, () -> {
                    });
                }
                break;
            case "place":
                switch (args.length) {
                    case 5: {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                        Storage.self.placeBlock(x, y, z, BlockFace.valueOf(args[4]));
                        break;
                    }
                    case 6: {
                        int x = Integer.parseInt(args[1]);
                        int y = Integer.parseInt(args[2]);
                        int z = Integer.parseInt(args[3]);
                        String type = args[4];
                        InventoryUtil.findAndSelect(Material.valueOf(type), 1);
                        Storage.self.placeBlock(x, y, z, BlockFace.valueOf(args[5]));
                        break;
                    }
                    default:
                        break;
                }
                break;
            case "abilities":
                if (args.length == 2) {
                    Storage.self.setAbilities(Integer.parseInt(args[1]));
                }
                break;
            case "action":
                if (args.length == 2) {
                    Storage.self.performAction(Integer.parseInt(args[1]));
                }
                break;
            case "icl":
                if (args.length == 3) {
                    Storage.self.getInventory().click(Integer.parseInt(args[1]), Integer.parseInt(args[2]), 0);
                }
                if (args.length == 2) {
                    Storage.self.getInventory().click(Integer.parseInt(args[1]), 0, 0);
                }
                break;
            case "icr":
                if (args.length == 2) {
                    Storage.self.getInventory().click(Integer.parseInt(args[1]), 0, 1);
                }
                break;
            case "pixel":
                TaskPixel tp = new TaskPixel(args);
                if (tp.load()) {
                    int delay = Integer.parseInt(args[4]);
                    int id = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, tp, delay, delay);
                    tp.identify(id);
                    break;
                }
            /*
                    case "run":
                    if (args.length >= 2) {
                    Task task = null;
                    String[] taskArgs = Arrays.copyOfRange(args, 2, args.length);
                    switch (args[1]) {
                    case "wall":
                    task = new TaskBuild(taskArgs);
                    break;
                    
                    if (task != null) {
                    int taskId = Storage.self.scheduleTask(Storage.plugin, task);
                    respond(respondTo, "Started Task " + taskId);
                    task.start();
                    }
                    }
                    break;
             */
            case "rm":
                if (args.length == 3) {
                    switch (args[1]) {
                        case "x":
                            double x = Double.parseDouble(args[2]);
                            Storage.self.moveTo(Storage.self.getLocation().getRelative(x, 0, 0));
                            break;
                        case "y":
                        case "why":
                            double y = Double.parseDouble(args[2]);
                            Storage.self.moveTo(Storage.self.getLocation().getRelative(0, y, 0));
                            break;
                        case "z":
                            double z = Double.parseDouble(args[2]);
                            Storage.self.moveTo(Storage.self.getLocation().getRelative(0, 0, z));
                            break;
                        case "t":
                            double t = Double.parseDouble(args[2]);
                            Storage.self.moveTo(Storage.self.getLocation().getRelative(0, 0, 0, t, 0));
                            break;
                        case "f":
                            double f = Double.parseDouble(args[2]);
                            Storage.self.moveTo(Storage.self.getLocation().getRelative(0, 0, 0, 0, f));
                            break;
                    }
                }
                break;
            case "sneak":
                Storage.self.sneak(true);
                break;
            case "unsneak":
                Storage.self.sneak(false);
                break;
            case "swing":
                Storage.self.swingArm(false);
                break;
            case "click":
                if (args.length == 4) {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);
                    Storage.self.clickBlock(x, y, z);
                }
                break;
            case "interact":
                Entity target = null;
                if (args.length == 2) {
                    int eid = Integer.parseInt(args[1]);
                    target = Storage.self.getEnvironment().getEntityById(eid);
                    if (target == null) {
                        respond(respondTo, "Can't see that entity");
                    }
                } else if (args.length == 1) {
                    double d = Double.MAX_VALUE;
                    double c;
                    Collection<Entity> ents = Storage.self.getEnvironment().getEntities();
                    for (Entity e : ents) {
                        c = e.getLocation().distanceTo(Storage.self.getLocation());
                        if (c < d) {
                            d = c;
                            target = e;
                        }
                    }
                }
                if (target != null) {
                    System.out.println("Interacting with " + target.getType() + " at " + target.getLocation());
                    Storage.self.interactWithEntity(target, false);
                }
                break;
            case "attack":
                target = null;
                Collection<Entity> ents = Storage.self.getEnvironment().getEntities();
                if (args.length == 2) {
                    int eid = Integer.parseInt(args[2]);
                    target = Storage.self.getEnvironment().getEntityById(eid);
                    if (target == null) {
                        respond(respondTo, "Can't see that entity");
                    }
                } else if (args.length == 1) {
                    double d = Double.MAX_VALUE;
                    double c;

                    for (Entity e : ents) {
                        if (!(e instanceof LivingEntity)) {
                            continue;
                        }
                        c = e.getLocation().distanceTo(Storage.self.getLocation());
                        if (c < d) {
                            d = c;
                            target = e;
                        }

                    }
                }
                if (target != null) {
                    System.out.println("Interacting with " + target.getType() + " at " + target.getLocation());
                    Storage.self.attackEntity(target);
                    Storage.self.swingArm(false);
                }
                break;
            case "shear":
                new TaskSpamClickEntitiesWithItem(
                        (e) -> {
                            return e.getType() == EntityType.SHEEP && !((Sheep) e).isSheared();
                        },
                        (is) -> {
                            return is.getType() == Material.SHEARS; // && durability < 200
                        }, 45).start();
                break;
            case "infinishear":
                if (Storage.infiniShear == null) {
                    Storage.infiniShear = new TaskInfiniShear(new Location(-977, 66, 4801), new Location(-977, 68, 4803), new Location(-976, 68, 4803));
                    Storage.infiniShear.start();
                    respond(respondTo, "Starting infinishear");
                } else {
                    Storage.infiniShear.disable();
                    respond(respondTo, "Depositing and disabling");
                    Storage.infiniShear = null;
                }
                break;
            case "bleach":
                new TaskSpamClickEntitiesWithItem(
                        (e) -> {
                            return e.getType() == EntityType.SHEEP;
                        },
                        (is) -> {
                            return is.getType() == Material.WHITE_DYE; // WHITE_DYE
                        }, 15).start();
                break;
            case "cows":
                new TaskSpamClickEntitiesWithItem(
                        (e) -> {
                            return e.getType() == EntityType.COW;
                        },
                        (is) -> {
                            return is.getType() == Material.WHEAT;
                        }, 15).start();
                break;
            case "nbt":
                ItemStack is = Storage.self.getInventory().getItemInHand();
                NBTBase k = is.getNbt();
                Storage.self.sendChat(k.toString());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                k.write(dos);
                dos.close();
                byte[] raw = bos.toByteArray();
                for (byte b : raw) {
                    System.out.printf("%02x ", b);
                }
                if (k.getId() != 10) {
                    Storage.self.sendChat("Root tag not a compound");
                    break;
                }
                NBTTagCompound l = (NBTTagCompound) k;
                k = l.getTag("ench");

                if (k.getId() != 9) {
                    Storage.self.sendChat("'ench' tag not a list");
                    break;
                }
                NBTTagList n = (NBTTagList) k;
                int count = n.tagCount();
                Storage.self.sendChat(count + " enchants:");
                for (int i = 0; i < count; i++) {
                    NBTTagCompound m = (NBTTagCompound) n.tagAt(i);
                    Storage.self.sendChat("ench " + i + ": id" + m.getShort("id") + " lvl " + m.getShort("lvl"));
                }
                break;
            case "ench":
                is = Storage.self.getInventory().getItemInHand();
                String enchString = EnchantEngine.stringifyItem(is);
                if (enchString.length() < 200) {
                    Storage.self.sendChat(enchString);
                } else {
                    Storage.self.sendChat("<" + enchString.length() + " characters>");
                    System.out.println(enchString);
                }
                break;
            case "lore":
                is = Storage.self.getInventory().getSlot(Storage.self.getInventory().getSelectedSlot());
                List<String> lore = is.getLore();

                if (lore == null || lore.isEmpty()) {
                    Storage.self.sendChat("This item has no lore");
                } else {
                    Storage.self.sendChat(lore.size() + " lines");
                }

                for (String line : lore) {
                    Storage.self.sendChat(line.replace("§", "&"));
                }
                break;
            case "name":
                is = Storage.self.getInventory().getSlot(Storage.self.getInventory().getSelectedSlot());
                String name = is.getDisplayName();

                if (name == null) {
                    Storage.self.sendChat("This item has no custom name");
                } else {
                    Storage.self.sendChat("This item is named " + name.replace("§", "&"));
                }
                break;
            case "index":
                Storage.self.sendChat(EnchantEngine.friendlyIndex(Integer.parseInt(args[1])));
                break;
            case "peeklib":
                int absoluteSlot = Integer.parseInt(args[1]);
                new TaskLookUpOneSlot(absoluteSlot).start();
                break;
            case "scanlib":
                new TaskScanLibrary().start();
                break;
            case "store_books":
                new TaskStoreBooks().start();
                break;
            case "fetchlib":
                if (args.length == 1) {
                    absoluteSlot = EnchantEngine.getQueryResult(EnchantEngine.getLastQueryIndex());
                } else {
                    absoluteSlot = Integer.parseInt(args[1]);
                }
                new TaskRetrieveOneItem(absoluteSlot).start();
                break;
            case "floor":
                new TaskFloor().start();
                break;
            case "walls":
                new TaskWalls().start();
                break;
            case "run":
                Class clazz = Class.forName("zbottestplugin.task." + args[0]);
                break;
            case "zombies":
                TaskZombieFilter filter = new TaskZombieFilter();
                if (Storage.zombieTaskId == -1) {
                    Storage.zombieTaskId = Storage.self.scheduleSyncRepeatingTask(Storage.plugin, filter, 0, 300);
                } else {
                    Storage.self.cancelTask(Storage.zombieTaskId);
                    Storage.zombieTaskId = -1;
                }
                break;
            case "heal":
                int zombieCount = 0;
                for (Entity e : Storage.self.getEnvironment().getEntities()) {
                    if (e instanceof ZombieVillager) {
                        zombieCount++;
                        Storage.self.interactWithEntity(e, false);
                    }
                }
                Storage.self.sendChat("Healed " + zombieCount + " zombie villagers");
                break;
            case "list_trades":
                Inventory inv = Storage.self.getInventory();
                if (!(inv instanceof VillagerInventory)) {
                    Storage.self.sendChat("Error: Inventory is type " + inv.getClass());
                    break;
                }
                VillagerInventory vInv = (VillagerInventory) inv;
                for (int i = 0; i < vInv.getNumTrades(); i++) {
                    Trade trade = vInv.getTrade(i);
                    Storage.self.sendChat("#" + i + ": " + (trade.getInput1().getAmount() + trade.getSpecialPrice()) + "x" + trade.getInput1().getType()
                            + "->" + trade.getOutput().getAmount() + "x" + trade.getOutput().getType());
                }
                break;
            case "select_trade":
                inv = Storage.self.getInventory();
                if (!(inv instanceof VillagerInventory)) {
                    Storage.self.sendChat("Error: Inventory is type " + inv.getClass());
                    break;
                }
                vInv = (VillagerInventory) inv;
                int selectTrade = Integer.parseInt(args[1]);
                if (selectTrade > vInv.getNumTrades()) {
                    Storage.self.sendChat("Error: Select 0-" + (vInv.getNumTrades() - 1));
                }
                vInv.selectTrade(selectTrade);
                break;
            case "welcome":
                if (args.length >= 2) {
                    String user = args[1];
                    Storage.self.scheduleSyncDelayedTask(Storage.plugin, () -> {
                        Storage.self.sendChat("/msg " + user + " Welcome, " + user + "!");
                    }, 2000);
                    Storage.self.scheduleSyncDelayedTask(Storage.plugin, () -> {
                        Storage.self.sendChat("/msg " + user + " We are a freebuild survival server");
                    }, 9000);
                    Storage.self.scheduleSyncDelayedTask(Storage.plugin, () -> {
                        Storage.self.sendChat("/msg " + user + " Make your way out of the spawn city to start building");
                    }, 16000);
                    Storage.self.scheduleSyncDelayedTask(Storage.plugin, () -> {
                        Storage.self.sendChat("/msg " + user + " Use one of the warps on the blue wall, or simply walk");
                    }, 23000);
                    Storage.self.scheduleSyncDelayedTask(Storage.plugin, () -> {
                        Storage.self.sendChat("/msg " + user + " No plots required, no griefing. Take a look at our /rules :)");
                    }, 30000);
                }
                break;
            case "fish":
                if (Storage.fish == null) {
                    Storage.fish = new TaskFish();
                    Storage.fish.start();
                    Storage.self.sendChat("Now fishing");
                } else {
                    Storage.fish.close();
                    Storage.fish = null;
                    Storage.self.sendChat("Enough fishing..");
                }
                break;
            case "close":
                Storage.self.closeWindow();
                break;
            case "window":
                inv = Storage.self.getInventory();
                if (inv instanceof FurnaceInventory) {
                    FurnaceInventory fi = (FurnaceInventory) inv;
                    Storage.self.sendChat("Furnace. Flame " + fi.getRemainingBurnTime() + "/" + fi.getMaxBurnTime() + " Arrow " + fi.getProgress() + "/" + fi.getMaxProgress());
                }
                break;
            case "camp":
            case "camp_pigs":
                TaskPigCamper.instance().start();
                break;
            case "sell_iron":
                new TaskSellIron().start();
                break;
            case "buy_lapis":
                new TaskBuyLapis().start();
                break;
            case "sell_books":
                new TaskSellBooks().start();
                break;
            case "buy_bookshelves":
                new TaskBuyBookShelves().start();
                break;
            case "exit":
                Storage.self.shutdown();
                break;
        }
    }

    private static void respond(String user, String message) {
        if (user == null) {
            Storage.self.sendChat(message);
        } else {
            Storage.self.sendChat("/msg " + user + " " + message);
        }
    }
}
