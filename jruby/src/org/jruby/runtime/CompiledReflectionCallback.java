
package org.jruby.runtime;

import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.Ruby;
import org.jruby.util.Asserts;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class CompiledReflectionCallback implements Callback {
    private Ruby runtime;
    private String methodName;
    private String className;
    private int arity;

    public CompiledReflectionCallback(Ruby runtime, String className, String methodName, int arity) {
        this.runtime = runtime;
        this.className = className;
        this.methodName = methodName;
        this.arity = arity;
    }

    public IRubyObject execute(IRubyObject recv, IRubyObject[] args) {
        Asserts.isTrue(arity == args.length);
        Object[] arguments = new Object[2 + args.length];
        arguments[0] = runtime;
        arguments[1] = recv;
        System.arraycopy(args, 0, arguments, 2, args.length);
        try {
            return (IRubyObject) getMethod().invoke(null, arguments);
        } catch (IllegalAccessException e) {
            Asserts.notReached(e.toString());
            return null;
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            } else {
                Asserts.notReached(e.getCause().toString());
                return null;
            }
        }
    }

    public Arity getArity() {
        return Arity.fixed(arity);
    }

    private Method getMethod() {
        try {
            Class javaClass = Class.forName(className);
            Class[] args = new Class[2 + arity];
            args[0] = Ruby.class;
            args[1] = IRubyObject.class;
            for (int i = 2; i < args.length; i++) {
                args[i] = IRubyObject.class;
            }
            return javaClass.getMethod(methodName, args);

        } catch (ClassNotFoundException e) {
            Asserts.notReached("class not found: " + className);
            return null;
        } catch (NoSuchMethodException e) {
            Asserts.notReached("method not found: " + methodName);
            return null;
        }
    }
}
