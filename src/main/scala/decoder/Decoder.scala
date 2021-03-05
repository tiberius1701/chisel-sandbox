// See README.md for license details.

package decoder

import chisel3._
import chisel3.util.BitPat


//data.value


class Instruction{
    var mnemonic: String = ""
    var formatted_mnemonic: String = ""
    var opcode: String = ""
    var formatted_opcode: String = ""
    var instruction_format: String = ""
    var full_name: String = ""
}

class Decoder extends Module {
  val io = IO(new Bundle {
    val insn   = Input(UInt(32.W))
    val opcode        = Output(UInt(10.W))
    val advance_state = Output(Bool())
    val opcode_found  = Output(Bool())
  })
  val jsonString = os.read(os.pwd/"src"/"main"/"scala"/"decoder"/"isa.json")
  val isa_list = ujson.read(jsonString)
  //val isa_list = data
  //val isa_list = ujson.read(jsonString).values
  //declare internals
  val opcode_found_internal = Bool()
  val opcode_internal = UInt(10.W)
  opcode_found_internal := false.B
  opcode_internal := false.B
  //for (insn <- isa_list){
  for (key <- isa_list.obj.keys){
      val insn = isa_list(key)
      when (BitPat("b"+insn("formatted_opcode").str.replace('-','?')) === io.insn){
        opcode_found_internal := true.B
        opcode_internal := opcode_enum.opcode_enum(insn("formatted_mnemonic").str).U
      }
  }
  
  when(opcode_found_internal) {
    io.opcode_found := true.B
    io.opcode := opcode_internal
    io.advance_state := true.B
  }.otherwise { 
    io.opcode_found := true.B
    io.advance_state := true.B
  }
}
