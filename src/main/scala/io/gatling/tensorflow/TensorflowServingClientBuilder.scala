package io.gatling.tensorflow

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.protocol.Protocols
import io.gatling.core.structure.ScenarioContext

case class TensorflowServingClientBuilder() extends ActionBuilder {
  def tensorflowServingClientProtocol(protocols: Protocols): TensorflowServingClientProtocol = {
    protocols.protocol[TensorflowServingClientProtocol]
      .getOrElse(throw new UnsupportedOperationException("TensorflowServingClientProtocol was not registered"))
  }

  override def build(ctx: ScenarioContext, next: Action): Action = {
    new TensorflowServingClientAction(
      tensorflowServingClientProtocol(ctx.protocolComponentsRegistry.protocols),
      ctx.coreComponents.statsEngine, next)
  }
}