package tip.analysis

import tip.ast.AstNodeData.DeclarationData
import tip.ast._
import tip.cfg.{CfgFunExitNode, CfgNode, CfgStmtNode, IntraproceduralProgramCfg}
import tip.lattices.{MapLattice, PowersetLattice}
import tip.solvers.{SimpleMapLatticeFixpointSolver, SimpleWorklistFixpointSolver}

abstract class ReachingDefinitionsAnalysis(cfg: IntraproceduralProgramCfg)(implicit declData: DeclarationData)
  extends FlowSensitiveAnalysis(false) {

  val lattice: MapLattice[CfgNode, PowersetLattice[AAssignStmt]] = new MapLattice(new PowersetLattice())

  val domain: Set[CfgNode] = cfg.nodes

  NoPointers.assertContainsProgram(cfg.prog)
  NoRecords.assertContainsProgram(cfg.prog)

  def transfer(n: CfgNode, s: lattice.sublattice.Element): lattice.sublattice.Element =
    n match {
      case _: CfgFunExitNode => lattice.sublattice.bottom
      case r: CfgStmtNode =>
        r.data match {
          case as: AAssignStmt =>
            as.left match {
              case id: AIdentifier => removeDef(s, id) + as
              case _ => s
            }
          case _ => s
        }
      case _ => s
    }

  private def removeDef(lat: lattice.sublattice.Element, defId: AIdentifier) = lat
    .filter(element => {
      element.left match {
        case id: AIdentifier => id.name != defId.name
        case _ => true
      }
    })
}

/**
 * Reaching Definitions analysis that uses the simple fixpoint solver.
 */
class ReachingDefinitionsAnalysisSimpleSolver(cfg: IntraproceduralProgramCfg)(implicit declData: DeclarationData)
  extends ReachingDefinitionsAnalysis(cfg)
    with SimpleMapLatticeFixpointSolver[CfgNode]
    with ForwardDependencies

/**
 * Reaching Definitions analysis that uses the worklist solver.
 */
class ReachingDefinitionsAnalysisWorklistSolver(cfg: IntraproceduralProgramCfg)(implicit declData: DeclarationData)
  extends ReachingDefinitionsAnalysis(cfg)
    with SimpleWorklistFixpointSolver[CfgNode]
    with ForwardDependencies
