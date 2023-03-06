//package nl.thomasbrants.mineroverview.modmenu;
//
//import com.terraformersmc.modmenu.api.ConfigScreenFactory;
//import com.terraformersmc.modmenu.api.ModMenuApi;
//import me.shedaniel.clothconfig2.api.ConfigBuilder;
//import net.minecraft.text.Text;
//
//public class ModMenuIntegration implements ModMenuApi {
//    @Override
//    public ConfigScreenFactory<?> getModConfigScreenFactory() {
//        return parent -> {
//            ConfigBuilder builder = ConfigBuilder.create()
//                .setParentScreen(parent)
//                .setTitle(Text.literal("Test"));
//
//            return builder.build();
//        };
//    }
//}
// "modmenu": [
//     "nl.thomasbrants.mineroverview.modmenu.ModMenuIntegration"
//     ]