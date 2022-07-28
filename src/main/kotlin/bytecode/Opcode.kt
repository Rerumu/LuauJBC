package bytecode

enum class Opcode {
    Nop,
    Break,

    LoadNil,
    LoadBoolean,
    LoadInteger,
    LoadConstant,

    Move,

    GetGlobal,
    SetGlobal,

    GetUpValue,
    SetUpValue,
    CloseUpValues,

    GetImport,

    GetTable,
    SetTable,
    GetTableKey,
    SetTableKey,
    GetTableIndex,
    SetTableIndex,

    NewClosure,

    NameCall,
    Call,
    Return,

    Jump,
    JumpSafe,

    JumpIfTruthy,
    JumpIfFalsy,
    JumpIfEqual,
    JumpIfLessEqual,
    JumpIfLessThan,
    JumpIfNotEqual,
    JumpIfMoreThan,
    JumpIfMoreEqual,

    Add,
    Sub,
    Mul,
    Div,
    Mod,
    Pow,

    AddConstant,
    SubConstant,
    MulConstant,
    DivConstant,
    ModConstant,
    PowConstant,

    And,
    Or,

    AndConstant,
    OrConstant,

    Concat,

    Not,
    Minus,
    Length,

    NewTable,
    DupTable,

    SetList,

    ForNumericPrep,
    ForNumericLoop,
    ForGenericLoop,

    ForGenericPrepINext,
    ForGenericLoopINext,

    ForGenericPrepNext,
    ForGenericLoopNext,

    GetVariadic,

    DupClosure,

    PrepVariadic,

    LoadConstantEx,

    JumpEx,

    FastCall,

    Coverage,
    Capture,

    JumpIfConstant,
    JumpIfNotConstant,

    FastCall1,
    FastCall2,
    FastCall2K,

    ForGenericPrep;

    fun length(): Int {
        return when (this) {
            GetGlobal,
            SetGlobal,
            GetImport,
            GetTableKey,
            SetTableKey,
            NameCall,
            JumpIfEqual,
            JumpIfLessEqual,
            JumpIfLessThan,
            JumpIfNotEqual,
            JumpIfMoreThan,
            JumpIfMoreEqual,
            NewTable,
            SetList,
            ForGenericLoop,
            LoadConstantEx,
            JumpIfConstant,
            JumpIfNotConstant,
            FastCall2,
            FastCall2K -> 2
            else -> 1
        }
    }
}
