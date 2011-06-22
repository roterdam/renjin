/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.parser.ParseUtil;

import java.io.PrintStream;
import java.util.List;

public class Print {

  private Print() {}


  public static void print(PrintStream printStream, SEXP value, int charactersPerLine) {
    printStream.println(new PrintingVisitor(value,charactersPerLine).getResult());
  }

  public static String print(SEXP expression, int charactersPerLine) {
    return new PrintingVisitor(expression,charactersPerLine).getResult();
  }

  @Primitive("print.default")
  public static void printDefault(@Current Context context, SEXP expression, SEXP digits, SEXP quote, SEXP naPrint,
                                    SEXP printGap, SEXP right, SEXP max, SEXP useSource, SEXP noOp) {

    String printed = new PrintingVisitor(expression,80).getResult();
    context.getGlobals().stdout.print(printed);
    context.getGlobals().stdout.flush();
  }

  @Primitive("print.function")
  public static void printFunction(@Current Context context, SEXP x, boolean useSource) {
    context.getGlobals().stdout.println(x.toString());
    context.getGlobals().stdout.flush();
  }

  static class PrintingVisitor extends SexpVisitor<String> {

    private StringBuilder out;
    private int charactersPerLine;

    PrintingVisitor(SEXP exp, int charactersPerLine) {
      this.out = new StringBuilder();
      this.charactersPerLine = charactersPerLine;

      exp.accept(this);
    }

    @Override
    public void visit(ListVector list) {
      int index = 1;
      for(int i=0; i!= list.length(); ++i) {
        SEXP value = list.get(i);
        String name = list.getName(i);

        if(name.length() == 0) {
          out.append("[[").append(index).append("]]\n");
        } else {
          out.append("$").append(name).append("\n");
        }
        value.accept(this);
        out.append("\n");
        index++;
      }
    }

    @Override
    public void visit(IntVector vector) {
      printVector(vector, Alignment.RIGHT, new ParseUtil.IntPrinter());
    }

    @Override
    public void visit(LogicalVector vector) {
      printVector(vector, Alignment.RIGHT, new ParseUtil.LogicalPrinter());
    }

    @Override
    public void visit(DoubleVector vector) {
      printVector(vector, Alignment.RIGHT, new ParseUtil.RealPrinter());
    }

    @Override
    public void visit(StringVector vector) {
      printVector(vector, Alignment.LEFT, new ParseUtil.StringPrinter());
    }

    @Override
    public void visit(Null nullExpression) {
      out.append("NULL\n");
    }

    @Override
    public void visitSpecial(SpecialFunction special) {
      out.append(".Primitive(").append(ParseUtil.formatStringLiteral(special.getName(), "NA"));
    }

    private <T> void printVector(Iterable<T> intExp, Alignment align, Function<T, String> printer) {
      List<String> elements = Lists.newArrayList(Iterables.transform(intExp, printer));
      new VectorPrinter(elements, align);
    }


    private enum Alignment {
      LEFT, RIGHT
    }

    private class VectorPrinter {
      private List<String> elements;
      private final Alignment elementAlign;
      private int maxElementWidth;
      private int maxIndexWidth;
      private int elementsPerLine;

      private VectorPrinter(List<String> elements, Alignment elementAlign) {
        this.elements = elements;
        this.elementAlign = elementAlign;
        calcMaxElementWidth();
        calcMaxIndexWidth();
        calcElementsPerLine();
        print();
      }

      private void calcMaxElementWidth() {
        for(String s : elements) {
          if(s.length() > maxElementWidth) {
            maxElementWidth = s.length();
          }
        }
      }

      private void calcMaxIndexWidth() {
        maxIndexWidth = (int)Math.ceil(Math.log10(elements.size()));
      }

      private void calcElementsPerLine() {
        elementsPerLine = (charactersPerLine - (maxIndexWidth+2)) / (maxElementWidth+1);
        if(elementsPerLine < 1) {
          elementsPerLine = 1;
        }
      }

      private void print() {
        int index = 0;
        while(index < elements.size()) {
          printIndex(index);
          printRow(index);
          index += elementsPerLine;
        }
      }

      private void printIndex(int index) {
        appendAligned(String.format("[%d]", index+1), maxIndexWidth+2, Alignment.RIGHT);
      }

      private void printRow(int startIndex) {
        for(int i=0;i!=elementsPerLine && (startIndex+i)<elements.size();++i) {
          out.append(' ');
          appendAligned(elements.get(startIndex+i), maxElementWidth, elementAlign);
        }
        out.append('\n');
      }

      private void appendAligned(String s, int size, Alignment alignment) {
        if(alignment == Alignment.LEFT) {
          out.append(s);
        }
        for(int i=s.length(); i<size; ++i) {
          out.append(' ');
        }
        if(alignment == Alignment.RIGHT) {
          out.append(s);

        }
      }
    }

    @Override
    protected void unhandled(SEXP exp) {
      out.append(exp.toString()).append('\n');
    }

    @Override
    public String getResult() {
      return out.toString();
    }
  }
}