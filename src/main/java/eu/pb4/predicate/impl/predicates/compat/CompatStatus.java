package eu.pb4.predicate.impl.predicates.compat;

public interface CompatStatus {
   /**
    * The upstream Fabric build gated these predicates on the separately installed Text
    * Placeholder API. This port bundles that API under
    * {@code dev.delesk1jx.styledchat.vendor.placeholders}, so it is always present and the
    * predicates are always registered.
    *
    * <p>The old check was {@code isModLoaded("placeholder-api")}, which never matched on Forge
    * because the loader id has no hyphen, so the {@code placeholder} predicate was silently
    * missing from every built-in set.
    */
   boolean PLACEHOLDER_API = true;

   /**
    * The permission predicates used to be gated on lucko's permissions API being installed. The
    * Forge port ships its own op-level backed implementation, so the predicates are always
    * available and existing configurations keep resolving.
    */
   boolean LUCKO_PERMISSION_API = true;
}
