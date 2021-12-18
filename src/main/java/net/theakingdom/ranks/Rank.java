package net.theakingdom.ranks;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

public class Rank implements CommandExecutor {
    Ranks plugin;

    public Rank(Ranks plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        label = "<subcommand> [<eventual args>]";
        MongoCollection coll = plugin.getColl();
        MongoCollection playerColl = plugin.playerColl;
        if(args.length < 1){
            if(!(sender instanceof Player)){
                sender.sendMessage("§cYou cannot use this subcommand.");
                return false;
            }
            Player p = (Player) sender;
            Document doc = new Document();
            doc.put("uuid", p.getUniqueId().toString());
            String rank = "Default";
            for(Object docObj : playerColl.find(doc)){
                Document tempDoc = (Document) docObj;
                String rankName = tempDoc.getString("rank");
                rank = rankName.substring(0,1).toUpperCase() + rankName.substring(1);
            }
            p.sendMessage("§8You are part of the current rank: §c" + rank);
            return true;
        }
        switch(args[0]){
            case "list":
                String end = "";
                int i = 0;
                for (Object obj : coll.find()){
                    Document doc = (Document) obj;
                    end += "§c" + doc.get("name") + "§8, ";
                }
                end = end.substring(0, end.lastIndexOf(", "));
                sender.sendMessage("§8List of all ranks:");
                sender.sendMessage(end.replaceAll("&", ""));
                break;
            case "create":
                if(args.length < 3) {
                    sender.sendMessage("§cYou need to add name and prefix.");
                    return false;
                }
                Document doc = new Document();
                ArrayList perms = new ArrayList<String>();
                doc.put("name", args[1]);
                doc.put("prefix", args[2]);
                doc.put("permissions", perms);
                coll.insertOne(doc);
                sender.sendMessage(("§8Rank with name §c" + args[1] + " §8has now been created with prefix §c\"" + args[2] +"§c\"§8.").replaceAll("&", "§"));
                break;
            case "delete":
                if(args.length < 2){
                    sender.sendMessage("§cYou need to add name.");
                    return false;
                }
                Document docu = new Document();
                docu.put("name", args[1]);
                coll.deleteOne(docu);
                sender.sendMessage(("§8Rank with name §c" + args[1] + " §8has now been deleted.").replaceAll("&", "§"));
                break;
            case "permission":
                switch(args[1]){
                    case "add":
                        if(args.length < 4){
                            sender.sendMessage("§cYou need to add name and permission.");
                            return false;
                        }
                        Document filterDoc = new Document();
                        filterDoc.put("name", args[2]);
                        FindIterable results = coll.find(filterDoc);
                        boolean found = false;
                        for(Object docObj : results){
                            if(!found) {
                                found = true;
                                Document docum = (Document) docObj;
                                ArrayList permsList = (ArrayList<String>) docum.get("permissions");
                                permsList.add(args[3]);
                                docum.put("permissions", permsList);
                                sender.sendMessage(("§8Permission &c" + args[3] + "§8 added to rank §c" + args[2] + "§8.").replaceAll("&", "§"));
                                Document tempDoc = new Document();
                                tempDoc.put("name", docum.get("name"));
                                coll.findOneAndReplace(tempDoc, docum);
                            }
                        }
                        if(!found){
                            sender.sendMessage("§cRank not found.");
                        }
                        break;
                    case "remove":
                        if(args.length < 4){
                            sender.sendMessage("§cYou need to add name and permission.");
                            return false;
                        }
                        Document filterDoc2 = new Document();
                        filterDoc2.put("name", args[2]);
                        FindIterable results2 = coll.find(filterDoc2);
                        boolean found2 = false;
                        for(Object docObj : results2){
                            if(!found2) {
                                found2 = true;
                                Document docum = (Document) docObj;
                                ArrayList permsList = (ArrayList<String>) docum.get("permissions");
                                permsList.remove(args[3]);
                                docum.put("permissions", permsList);
                                sender.sendMessage(("§8Permission §c" + args[3] + "§8 removed from rank §c" + args[2] + "§8.").replaceAll("&", "§"));
                                Document tempDoc = new Document();
                                tempDoc.put("name", docum.get("name"));
                                coll.findOneAndReplace(tempDoc, docum);
                            }
                        }
                        if(!found2){
                            sender.sendMessage("§cRank not found.");
                        }
                        break;
                }
                break;
            case "add":
                if(args.length < 3){
                    sender.sendMessage("§cYou need to add player and rank.");
                    return false;
                }
                Document tempDoc = new Document();
                OfflinePlayer op = Bukkit.getOfflinePlayerIfCached(args[1]);
                tempDoc.put("uuid", op.getUniqueId().toString());
                for(Object playerObj : playerColl.find(tempDoc)){
                    sender.sendMessage("§cThis person already exists.");
                    return false;
                }
                Document scndTmpDoc = new Document();
                scndTmpDoc.put("name", args[2]);
                boolean found = false;
                for(Object docObj : coll.find(scndTmpDoc)){
                    if(!found) {
                        found = true;
                        tempDoc.put("rank", args[2]);
                        playerColl.insertOne(tempDoc);
                        sender.sendMessage("§c" + args[1] + " §8is now part of rank §c" + args[2] + "§8.");
                    }
                }
                if(!found){
                    sender.sendMessage("§cRank not found.");
                }
                break;
            case "remove":
                if(args.length < 2){
                    sender.sendMessage("§cYou need to add player.");
                    return false;
                }
                op = Bukkit.getOfflinePlayerIfCached(args[1]);
                tempDoc = new Document();
                tempDoc.put("uuid", op.getUniqueId().toString());
                playerColl.deleteOne(tempDoc);
                sender.sendMessage("§8Removed player §c" + args[1] + "§8 from rank they were part of.");
                break;
            case "prefix":
                if(args.length < 2){
                    sender.sendMessage("§cYou need to add rank.");
                    return false;
                }
                tempDoc = new Document();
                found = false;
                tempDoc.put("name", args[1]);
                for(Object docObj : coll.find(tempDoc)){
                    doc = (Document) docObj;
                    if(!found){
                        found = true;
                        sender.sendMessage("§8Prefix of rank §c" + args[1] + " §8is §c" + doc.get("prefix").toString().replaceAll("&", "§") + "§8.");
                    }
                }
                if(!found){
                    sender.sendMessage("§cThis rank was not found.");
                }
                break;
        }
        return true;
    }
}
