// See README.md for license details.

package decoder

import chisel3._
import chisel3.tester._
import org.scalatest.FreeSpec
import chisel3.experimental.BundleLiterals._

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GcdDecoupledTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GcdDecoupledTester'
  * }}}
  */
class DecoderSpec extends FreeSpec with ChiselScalatestTester {

  "Decoder should output correct opcode" in {
    test(new DecoupledDecoder) { dut =>
      dut.input.initSource()
      dut.input.setSourceClock(dut.clock)
      dut.output.initSink()
      dut.output.setSinkClock(dut.clock)

      //val testValues = for { x <- 0 to 10; y <- 0 to 10} yield (x, y)
      val testValues = List(
        "b_00001000000000000000000000000000",
        "b_00001100000000000000000000000000",
        "b_00010000000000000000000000000000"
      )
      val answerKey = List(0, 1, 678)
      val inputSeq = testValues.map { x => (new DecoderInputBundle).Lit(_.insn -> x.U) }
      val resultSeq = answerKey.map { x =>
        (new DecoderOutputBundle).Lit(_.advance_state -> true.B, _.opcode -> x.U)
      }

      fork {
        // push inputs into the calculator, stall for 11 cycles one third of the way
        val (seq1, seq2) = inputSeq.splitAt(resultSeq.length / 3)
        dut.input.enqueueSeq(seq1)
        dut.clock.step(11)
        dut.input.enqueueSeq(seq2)
      }.fork {
        // retrieve computations from the calculator, stall for 10 cycles one half of the way
        val (seq1, seq2) = resultSeq.splitAt(resultSeq.length / 2)
        dut.output.expectDequeueSeq(seq1)
        dut.clock.step(10)
        dut.output.expectDequeueSeq(seq2)
      }.join()

    }
  }
}
