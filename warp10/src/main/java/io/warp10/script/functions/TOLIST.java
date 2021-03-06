//
//   Copyright 2016  Cityzen Data
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Make a list with 'N' elements present in the stack.
 * Replace those 'N' elements and 'N' with the list.
 * 
 * @param element1 First element of the resulting list
 * @param element2
 * @param elementN Last element of the resulting list
 * @param N Number of elements to add to the list
 */
public class TOLIST extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  
  public TOLIST(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    Object[] elements = stack.popn();
    List<Object> list = new ArrayList<Object>();
    list.addAll(Arrays.asList(elements));
    stack.push(list);
    return stack;
  }
}
