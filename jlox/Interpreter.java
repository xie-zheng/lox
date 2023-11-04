package jlox;

import jdk.incubator.vector.VectorOperators.Ternary;
import java.util.List;

class Interpreter implements Expr.Visitor<Object>,
							 Stmt.Visitor<Object> {
	private Object evaluate(Expr expr) {
    	return expr.accept(this);
  	}

	private void execute(Stmt stmt) {
		stmt.accept(this);
	}

	private boolean isEqual(Object left, Object right) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null) {
			return false;
		}
		return left.equals(right);
	}
	
	private boolean isTruthy(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof Boolean) {
			return (boolean)obj;
		}
		return true;
	}

	private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) {
			return;
		}
		throw new RuntimeError(operator, "Operand must be a number.");
	}

	private String stringify(Object object) {
    	if (object == null) {
			return "nil";
		}

	    if (object instanceof Double) {
    		String text = object.toString();
      		if (text.endsWith(".0")) {
        		text = text.substring(0, text.length() - 2);
      		}
      		return text;
    	}
    	return object.toString();
  	}

	@Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

	@Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
    	System.out.println(stringify(value));
    	return null;
  	}

	@Override
	public Object visitLiteralExpr(Expr.Literal expr) {
		return expr.value;
	}

  	@Override
  	public Object visitGroupingExpr(Expr.Grouping expr) {
    	return evaluate(expr.expression);
  	}

	@Override
  	public Object visitUnaryExpr(Expr.Unary expr) {
    	Object right = evaluate(expr.right);
		switch (expr.operator.type) {
      		case MINUS:
				checkNumberOperand(expr.operator, right);
        		return -(double)right;
			case BANG:
				return !isTruthy(right);
			default:
				break;
    	}
	    // Unreachable.
    	return null;
  	}
	
	private Object binaryExprDouble(Token operator, double left, double right) {
		switch (operator.type) {
			case GREATER:
		    	return left > right;
	    	case GREATER_EQUAL:
    			return left >= right;
      		case LESS:
        		return left < right;
      		case LESS_EQUAL:
        		return left <= right;
    		case MINUS:
        		return left - right;
			case PLUS:
          		return left + right;
		    case SLASH:
        		return left / right;
      		case STAR:
        		return left * right;
			default:
				break;
		}
		return null;
	}
	
	@Override
	public Object visitBinaryExpr(Expr.Binary expr) {
    	Object left = evaluate(expr.left);
    	Object right = evaluate(expr.right);
		
	    switch (expr.operator.type) {
		    case BANG_EQUAL:  return !isEqual(left, right);
      		case EQUAL_EQUAL: return isEqual(left, right);
			default:
				break;
    	}

		if (left instanceof Double && right instanceof Double) {
			return binaryExprDouble(expr.operator, (double)left, (double)right);
		}

		if (left instanceof String && right instanceof String) {
			if (expr.operator.type == TokenType.PLUS) {
				return (String)left + (String)right;
			}
		}

		throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
    	// Unreachable.
    	// return null;
  	}

	void interpret(List<Stmt> statements) { 
    	try {
			for (Stmt statement: statements) {
				execute(statement);
			}
   	 	} catch (RuntimeError error) {
      		Lox.runtimeError(error);
    	}
  	}
}
