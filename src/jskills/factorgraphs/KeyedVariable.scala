// Generated by delombok at Mon Sep 05 01:05:25 CEST 2011
package jskills.factorgraphs;
import scala.reflect.BeanProperty

class KeyedVariable[K, V](@BeanProperty val key: K, prior: V, name: String, args: Any*)
  extends Variable[V](prior, name, args)