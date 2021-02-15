package nomoreunitplacevialogicblock;

import arc.Events;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.net.NetConnection;
import mindustry.world.blocks.logic.LogicBlock;
import pluginutil.GHPlugin;

import java.lang.reflect.Method;

import static pluginutil.PluginUtil.SendMode.*;

@SuppressWarnings("unused")
public class NoMoreUnitPlaceViaLogicBlock extends GHPlugin {

    public void init() {
        super.init();

        Events.on(EventType.TileChangeEvent.class, event -> {
            if (!(event.tile.build instanceof LogicBlock.LogicBuild)) return;
            cleanUp((LogicBlock.LogicBuild) event.tile.build);
        });

        Events.on(EventType.ConfigEvent.class, event -> {
            if (!(event.tile instanceof LogicBlock.LogicBuild)) return;
            cleanUp((LogicBlock.LogicBuild) event.tile);
        });

        log(info, "Initialized");
    }

    private void cleanUp(LogicBlock.LogicBuild logic){
        if (logic.executor.instructions == null || logic.executor.instructions.length == 0)
            return;

        if (!logic.code.matches("(?s).*ucontrol.*"))
            return;

        logic.updateCode(logic.code.replaceAll("ucontrol.*\n?", ""));

        Player player = Groups.player.find(p -> p.name.equals(logic.lastAccessed));
        if (player != null)
            player.kick(cfg().kickReason);

//        log("Modified Logic Block. [" + logic.tile.x + ", " + logic.tile.y + ", " + logic.lastAccessed + "]");
        for (Player player1 : Groups.player) {
            if (player1.admin)
                player1.sendMessage("Modified Logic Block. [" + logic.tile.x + ", " + logic.tile.y + ", " + logic.lastAccessed + "]");
        }
    }

    protected void defConfig() {
        this.cfg = new NoMoreUnitPlaceViaLogicBlockConfig();
    }

    protected NoMoreUnitPlaceViaLogicBlockConfig cfg() {
        return (NoMoreUnitPlaceViaLogicBlockConfig) cfg;
    }

    protected static class NoMoreUnitPlaceViaLogicBlockConfig extends GHPluginConfig{
        private String kickReason;

        @Override
        public void reset() {
            kickReason = "Unit Control in Logic Block is Prohibited in this server due to being a common griefing method.";
        }
    }
}
