package tip.analysis

import tip.ast.AstNodeData.DeclarationData
import tip.cfg.{CfgNode, IntraproceduralProgramCfg, ProgramCfg}
import tip.lattices.IntervalLattice.{IntNum, Num}
import tip.lattices.{IntervalLattice, LiftLattice}
import tip.solvers.{WorklistFixpointSolverWithReachabilityAndWidening, WorklistFixpointSolverWithReachabilityAndWideningAndNarrowing}

import scala.math.pow

trait SizesAnalysis extends ValueAnalysisMisc with Dependencies[CfgNode] {
  val cfg: ProgramCfg

  val valuelattice: IntervalLattice.type

  val liftedstatelattice: LiftLattice[statelattice.type]

  /**
   * Int values occurring in the program, plus -infinity and +infinity.
   */
  private val B = {
    var result = Set[Num]()

    for (i <- 1 to 31) {
      result += -pow(2, i).intValue
      result += pow(2, i).intValue - 1
    }

    result
  }

  def loophead(n: CfgNode): Boolean = indep(n).exists(cfg.rank(_) > cfg.rank(n))

  private def minB(b: IntervalLattice.Num) = B.filter(b <= _).min

  private def maxB(a: IntervalLattice.Num) = B.filter(_ <= a).max

  def widenInterval(x: valuelattice.Element, y: valuelattice.Element): valuelattice.Element = {
    (x, y) match {
      case (IntervalLattice.EmptyInterval, _) => y
      case (_, IntervalLattice.EmptyInterval) => x
      case ((l1, h1), (l2, h2)) => (
        if (l1 < l2) l1 else maxB(l2),
        if (h2 < h1) h1 else minB(h2)
      )
    }
  }

  def widen(x: liftedstatelattice.Element, y: liftedstatelattice.Element): liftedstatelattice.Element =
    (x, y) match {
      case (liftedstatelattice.Bottom, _) => y
      case (_, liftedstatelattice.Bottom) => x
      case (liftedstatelattice.Lift(xm), liftedstatelattice.Lift(ym)) =>
        liftedstatelattice.Lift(declaredVars.map { v =>
          v -> widenInterval(xm(v), ym(v))
        }.toMap)
    }
}

class SizesAnalysisWithWidening(cfg: IntraproceduralProgramCfg)(implicit declData: DeclarationData)
  extends IntraprocValueAnalysisWorklistSolverWithReachability(cfg, IntervalLattice)
    with WorklistFixpointSolverWithReachabilityAndWidening[CfgNode]
    with IntervalAnalysisWidening

class SizesAnalysisWithWideningAndNarrowing(cfg: IntraproceduralProgramCfg)(implicit declData: DeclarationData)
  extends IntraprocValueAnalysisWorklistSolverWithReachability(cfg, IntervalLattice)
    with WorklistFixpointSolverWithReachabilityAndWideningAndNarrowing[CfgNode]
    with IntervalAnalysisWidening {

  val narrowingSteps = 5
}
