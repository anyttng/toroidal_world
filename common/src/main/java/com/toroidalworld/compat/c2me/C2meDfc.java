package com.toroidalworld.compat.c2me;

import com.mojang.logging.LogUtils;
import com.toroidalworld.compat.ModPresence;
import com.toroidalworld.compat.ModSymbol;

public final class C2meDfc {
    static final ModSymbol AST_REGISTRY = new ModSymbol(
            "com/ishland/c2me/opts/dfc/common/ast/McToAst", "REGISTRY",
            "Lcom/ishland/c2me/opts/dfc/common/ast/FrontendRegistry;");

    private static final ModPresence GATE = ModPresence.of(LogUtils.getLogger(),
            "com/ishland/c2me/opts/dfc/mixin/MixinNoiseConfig.class",
            "[c2me-compat] gate dfc_present", AST_REGISTRY);

    public static boolean present() {
        return GATE.present();
    }

    private C2meDfc() {
    }
}
