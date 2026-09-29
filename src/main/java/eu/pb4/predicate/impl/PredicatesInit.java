package eu.pb4.predicate.impl;

import eu.pb4.predicate.api.PredicateRegistry;
import eu.pb4.predicate.impl.predicates.compat.CompatStatus;
import eu.pb4.predicate.impl.predicates.compat.PermissionOptionPredicate;
import eu.pb4.predicate.impl.predicates.compat.PermissionPredicate;
import eu.pb4.predicate.impl.predicates.compat.PlaceholderPredicate;
import eu.pb4.predicate.impl.predicates.generic.AllPredicate;
import eu.pb4.predicate.impl.predicates.generic.AnyPredicate;
import eu.pb4.predicate.impl.predicates.generic.EqualityPredicate;
import eu.pb4.predicate.impl.predicates.generic.NegatePredicate;
import eu.pb4.predicate.impl.predicates.generic.NumberPredicate;
import eu.pb4.predicate.impl.predicates.player.EntityPredicatePredicate;
import eu.pb4.predicate.impl.predicates.player.OperatorPredicate;
import eu.pb4.predicate.impl.predicates.player.StatisticPredicate;

public class PredicatesInit {
   public PredicatesInit() {
   }

   public static void initialize() {
      PredicateRegistry.register(EqualityPredicate.ID, EqualityPredicate.CODEC);
      PredicateRegistry.register(NegatePredicate.ID, NegatePredicate.CODEC);
      PredicateRegistry.register(AnyPredicate.ID, AnyPredicate.CODEC);
      PredicateRegistry.register(AllPredicate.ID, AllPredicate.CODEC);
      PredicateRegistry.register(NumberPredicate.LessThan.ID, NumberPredicate.LessThan.CODEC);
      PredicateRegistry.register(NumberPredicate.LessEqual.ID, NumberPredicate.LessEqual.CODEC);
      PredicateRegistry.register(NumberPredicate.MoreThan.ID, NumberPredicate.MoreThan.CODEC);
      PredicateRegistry.register(NumberPredicate.MoreEqual.ID, NumberPredicate.MoreEqual.CODEC);
      PredicateRegistry.register(OperatorPredicate.ID, OperatorPredicate.CODEC);
      PredicateRegistry.register(StatisticPredicate.ID, StatisticPredicate.CODEC);
      PredicateRegistry.register(EntityPredicatePredicate.ID, EntityPredicatePredicate.CODEC);
      if (CompatStatus.PLACEHOLDER_API) {
         PredicateRegistry.register(PlaceholderPredicate.ID, PlaceholderPredicate.CODEC);
      }

      if (CompatStatus.LUCKO_PERMISSION_API) {
         PredicateRegistry.register(PermissionPredicate.ID, PermissionPredicate.CODEC);
         PredicateRegistry.register(PermissionOptionPredicate.ID, PermissionOptionPredicate.CODEC);
      }
   }
}
