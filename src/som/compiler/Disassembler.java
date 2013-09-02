/**
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.compiler;

import som.interpreter.Bytecodes;
import som.vm.Universe;
import som.vmobjects.Class;
import som.vmobjects.Invokable;
import som.vmobjects.Method;
import som.vmobjects.Object;
import som.vmobjects.Symbol;

public class Disassembler {

  public static void dump(Class cl) {
    for (int i = 0; i < cl.getNumberOfInstanceInvokables(); i++) {
      Invokable inv = cl.getInstanceInvokable(i);

      // output header and skip if the Invokable is a Primitive
      Universe.errorPrint(cl.getName().toString() + ">>"
          + inv.getSignature().toString() + " = ");

      if (inv.isPrimitive()) {
        Universe.errorPrintln("<primitive>");
        continue;
      }
      // output actual method
      dumpMethod((Method) inv, "\t");
    }
  }

  public static void dumpMethod(Method m, java.lang.String indent) {
    Universe.errorPrintln("(");

    // output stack information
    Universe.errorPrintln(indent + "<" + m.getNumberOfLocals() + " locals, "
        + m.getMaximumNumberOfStackElements() + " stack, "
        + m.getNumberOfBytecodes() + " bc_count>");

    // output bytecodes
    for (int b = 0;
         b < m.getNumberOfBytecodes();
         b += Bytecodes.getBytecodeLength(m.getBytecode(b))) {

      Universe.errorPrint(indent);

      // bytecode index
      if (b < 10)  { Universe.errorPrint(" "); }
      if (b < 100) { Universe.errorPrint(" "); }
      Universe.errorPrint(" " + b + ":");

      // mnemonic
      byte bytecode = m.getBytecode(b);
      Universe.errorPrint(Bytecodes.bytecodeNames[bytecode] + "  ");

      // parameters (if any)
      if (Bytecodes.getBytecodeLength(bytecode) == 1) {
        Universe.errorPrintln();
        continue;
      }
      switch (bytecode) {
        case Bytecodes.push_local:
          Universe.errorPrintln("local: " + m.getBytecode(b + 1) + ", context: "
              + m.getBytecode(b + 2));
          break;
        case Bytecodes.push_argument:
          Universe.errorPrintln("argument: " + m.getBytecode(b + 1) + ", context "
              + m.getBytecode(b + 2));
          break;
        case Bytecodes.push_field:
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1) + ") field: "
              + ((Symbol) m.getConstant(b)).toString());
          break;
        case Bytecodes.push_block:
          Universe.errorPrint("block: (index: " + m.getBytecode(b + 1) + ") ");
          dumpMethod((Method) m.getConstant(b), indent + "\t");
          break;
        case Bytecodes.push_constant:
          Object constant = m.getConstant(b);
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1) + ") value: "
              + "(" + constant.getSOMClass().getName().toString() + ") "
              + constant.toString());
          break;
        case Bytecodes.push_global:
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1) + ") value: "
              + ((Symbol) m.getConstant(b)).toString());
          break;
        case Bytecodes.pop_local:
          Universe.errorPrintln("local: " + m.getBytecode(b + 1) + ", context: "
              + m.getBytecode(b + 2));
          break;
        case Bytecodes.pop_argument:
          Universe.errorPrintln("argument: " + m.getBytecode(b + 1)
              + ", context: " + m.getBytecode(b + 2));
          break;
        case Bytecodes.pop_field:
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1) + ") field: "
              + ((Symbol) m.getConstant(b)).toString());
          break;
        case Bytecodes.send:
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1)
              + ") signature: " + ((Symbol) m.getConstant(b)).toString());
          break;
        case Bytecodes.super_send:
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1)
              + ") signature: " + ((Symbol) m.getConstant(b)).toString());
          break;
        default:
          Universe.errorPrintln("<incorrect bytecode>");
      }
    }
    Universe.errorPrintln(indent + ")");
  }

}

