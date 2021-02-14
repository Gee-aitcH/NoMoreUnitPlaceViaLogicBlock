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

    private Method tileConfig__forward;

    protected void defConfig() {}

    public void init() {
        Events.on(EventType.TileChangeEvent.class, event -> {
            if (!(event.tile.build instanceof LogicBlock.LogicBuild)) return;
            cleanUp((LogicBlock.LogicBuild) event.tile.build);
        });

        Events.on(EventType.ConfigEvent.class, event -> {
            if (!(event.tile instanceof LogicBlock.LogicBuild)) return;
            cleanUp((LogicBlock.LogicBuild) event.tile);
        });

        try{
            tileConfig__forward = Call.class.getDeclaredMethod("tileConfig__forward", NetConnection.class, Player.class, Building.class, Object.class);
            tileConfig__forward.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        log(info, "Initialized");
    }

    private void cleanUp(LogicBlock.LogicBuild logic){
        if (logic.executor.instructions == null || logic.executor.instructions.length == 0)
            return;

        if (!logic.code.matches("(?s).*ucontrol.*"))
            return;

        logic.updateCode(logic.code.replaceAll("ucontrol.*\n?", ""));

        try {
            if (tileConfig__forward != null) {
                Player player = Groups.player.find(p -> p.name.equals(logic.lastAccessed));
                tileConfig__forward.invoke(null, player == null || player.con == null ? null : player.con, player, logic, logic.config());
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        log("Modified Logic Block. [" + logic.tile.x + ", " + logic.tile.y + ", " + logic.lastAccessed + "]");
    }
}
