package decoder

import chisel3._
import chisel3.util.Decoupled
import chisel3.util.BitPat



class DecoderInputBundle extends Bundle {
  val insn   = UInt(32.W)
}

class DecoderOutputBundle extends Bundle {
  val opcode        = UInt(10.W)
  val advance_state = Bool()
}

class DecoupledDecoder extends MultiIOModule {
  val input = IO(Flipped(Decoupled(new DecoderInputBundle)))
  val output = IO(Decoupled(new DecoderOutputBundle))

  val busy        = RegInit(false.B)
  val opcode_found  = RegInit(false.B)
  val opcode_found_internal = RegInit(false.B)
  val opcode_internal = Reg(UInt(10.W))

  val insn_in     = RegInit(0.U(32.W))
  
  val jsonString = os.read(os.pwd/"src"/"main"/"scala"/"decoder"/"isa.json")
  val isa_list = ujson.read(jsonString)
  //val isa_list = data.value
  //println(isa_list)
  //for (k <- isa_list("twi")){
   //   println(k)
  //}
  /*for (key <- isa_list.obj.keys){
      val insn = isa_list(key)
      println(insn("formatted_opcode"))
  }*/
  //println(isa_list.obj.keys)
  //println(isa_list("twi")("opcode").value)

  //println((data("twi"))("mnemonic").value)
  
  //val resultValid = RegInit(false.B)

  input.ready := ! busy
  output.valid := opcode_found
  output.bits := DontCare

  when(busy)  {
    for (key <- isa_list.obj.keys){
      val insn = isa_list(key)
    //for (insn <- isa_list){
    //for ((k, insn) <- isa_list.value){
      when (BitPat("b"+insn("formatted_opcode").str.replace('-','?')) === insn_in){
        opcode_found_internal := true.B
        opcode_internal := opcode_enum.opcode_enum(insn("formatted_mnemonic").str).U
      }
    }
    when (opcode_found_internal){
      output.bits.opcode := opcode_internal
      output.bits.advance_state := true.B
      opcode_found := true.B
      when (output.ready && opcode_found){
          busy := false.B
          opcode_found := false.B
      }
    }
  }.otherwise {
    when(input.valid) {
      val bundle = input.deq()
      insn_in := bundle.insn
      opcode_found_internal := false.B
      busy := true.B
    }
  }
}