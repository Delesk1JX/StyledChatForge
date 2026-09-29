package eu.pb4.predicate.impl.predicates.compat;

import eu.pb4.loader.Platform;

public interface CompatStatus {
   boolean PLACEHOLDER_API = Platform.isModLoaded("placeholder-api");

   /**
    * The permission predicates used to be gated on lucko's permissions API being installed. The
    * Forge port ships its own op-level backed implementation, so the predicates are always
    * available and existing configurations keep resolving.
    */
   boolean LUCKO_PERMISSION_API = true;
}
