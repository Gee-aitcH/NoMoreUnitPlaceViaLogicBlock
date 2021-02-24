package nomoreunitplacevialogicblock;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import com.google.gson.Gson;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.blocks.logic.LogicBlock;
import pluginutil.GHPlugin;

import static pluginutil.PluginUtil.f;

@SuppressWarnings("unused")
public class NoMoreUnitPlaceViaLogicBlock extends GHPlugin {

    public void init() {
        super.init();
        log(new Gson().toJson(cfg));

        Events.on(EventType.TileChangeEvent.class, event -> {
            if (!(event.tile.build instanceof LogicBlock.LogicBuild)) return;
            cleanUp((LogicBlock.LogicBuild) event.tile.build);
        });

        Events.on(EventType.ConfigEvent.class, event -> {
            if (!(event.tile instanceof LogicBlock.LogicBuild)) return;
            cleanUp((LogicBlock.LogicBuild) event.tile);
        });

        log("Initialized");
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("unitlogic", "<reload>", "No More Unit Place Via Logic Block", args ->{
            String arg = args[0];
            if (arg.equals("reload")) {
                read();
                log("Reloaded");
            }
        });
    }

    private void cleanUp(LogicBlock.LogicBuild logic){
        if (logic.executor.instructions == null || logic.executor.instructions.length == 0)
            return;

        if (!logic.code.matches("(?s).*" + cfg().detector + ".*"))
            return;

        logic.updateCode(logic.code.replaceAll(cfg().detector + ".*\n?", cfg().replaceWith));

        Player player = Groups.player.find(p -> p.name.equals(logic.lastAccessed));
        if (player != null && cfg().kick) {
            player.kick(cfg().kickReason);
            log(f("Kicked [%s] for putting [%s] in logic block.", player.name, cfg().detector));
        }

        Call.tileConfig(null, logic, logic.config());

        for (Player player1 : Groups.player) {
            if (player1.admin)
                player1.sendMessage("Modified logic block. [" + logic.tile.x + ", " + logic.tile.y + ", " + logic.lastAccessed + "]");
        }
    }

    protected void defConfig() {
        this.cfg = new NoMoreUnitPlaceViaLogicBlockConfig();
    }

    @SuppressWarnings("unchecked")
    protected NoMoreUnitPlaceViaLogicBlockConfig cfg() {
        return (NoMoreUnitPlaceViaLogicBlockConfig) cfg;
    }

    protected static class NoMoreUnitPlaceViaLogicBlockConfig extends GHPluginConfig{
        private String detector;
        private String replaceWith;
        private Boolean kick;
        private String kickReason;

        @Override
        protected void reset() {
            detector = "ucontrol build";
            replaceWith = "noop";
            kick = true;
            kickReason = "[scarlet]Unit Control in Logic Block is Prohibited in this server due to being a common griefing method.";
        }

        @Override
        public boolean softReset(){
            boolean modified = false;
            if(detector == null) {
                detector = "ucontrol build";
                modified = true;
            }

            if(replaceWith == null) {
                replaceWith = "noop";
                modified = true;
            }

            if(kick == null) {
                kick = true;
                modified = true;
            }

            if(kickReason == null) {
                kickReason = "[scarlet]Unit Control in Logic Block is Prohibited in this server due to being a common griefing method.";
                modified = true;
            }

            Log.info("Modified: " + modified);
            return modified;
        }

    }
}
