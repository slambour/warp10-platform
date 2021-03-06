//
//   Copyright 2017  Cityzen Data
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package io.warp10.script.functions;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptStackFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStack.Macro;

public class TRY extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  public TRY(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    Object o = stack.pop();
    
    if (!(o instanceof Macro)) {
      throw new WarpScriptException(getName() + " expects a 'finally' macro on top of the stack.");
    }
    
    Macro finallyMacro = (Macro) o;
    
    o = stack.pop();

    if (!(o instanceof Macro)) {
      throw new WarpScriptException(getName() + " expects a 'catch' macro below the 'finally' macro.");
    }
    
    Macro catchMacro = (Macro) o;
    
    o = stack.pop();
    
    if (!(o instanceof Macro)) {
      throw new WarpScriptException(getName() + " expects a 'try' macro below the 'catch' macro.");
    }
    
    Macro tryMacro = (Macro) o;
    
    try {
      stack.exec(tryMacro);
    } catch (Throwable t) {
      stack.setAttribute(WarpScriptStack.ATTRIBUTE_LAST_ERROR, t);
      stack.exec(catchMacro);
    } finally {
      stack.exec(finallyMacro);
    }

    return stack;
  }
}
