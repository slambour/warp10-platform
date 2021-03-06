package io.warp10.script.ext.sensision;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;
import io.warp10.sensision.Sensision;

import java.util.List;
import java.util.Map;

/**
 * Sets a Sensision metric
 */
public class SENSISIONSET extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  public SENSISIONSET(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    Object top = stack.pop();

    if (!(top instanceof List)) {
      throw new WarpScriptException(getName() + " expects a list on top of the stack.");
    }
    
    List<Object> args = (List<Object>) top;
        
    String cls = args.get(0).toString();
    Map<String,String> labels = (Map<String,String>) args.get(1);
    Object value = args.get(2);
    
    Long ttl = null;
    
    if (args.size() > 3) {
      ttl = ((Number) args.get(3)).longValue();
    }
    
    Sensision.set(cls, labels, value, ttl);

    return stack;
  }
  
}
