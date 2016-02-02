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

import io.warp10.continuum.gts.GTSOutliersHelper;
import io.warp10.continuum.gts.GeoTimeSerie;
import io.warp10.script.GTSStackFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Apply an STL-ESD test
 * 
 * Alpha is optional. Default value is 0.05.
 * STL parameters are optional. They can be set by adding a map as last parameter.
 */
public class STLESDTEST extends GTSStackFunction {

  private static final String PERIOD_PARAM = "bpp";
  private static final String UPPERBOUND_PARAM = "k";
  private static final String SIGNIFICANCE_PARAM = "alpha";
  
  private static final double SIGNIFICANCE_DEFAULT = 0.05D;
    
  public STLESDTEST(String name) {
    super(name);
  }
  
  @Override
  protected Map<String, Object> retrieveParameters(WarpScriptStack stack) throws WarpScriptException {
    Map<String,Object> params = null;
    
    Object top = stack.pop();
    
    // Reusing optional map is given as argument
    if (!(top instanceof Map)) {
      params = new HashMap<String,Object>();
    } else {
      params = (Map<String,Object>) top;
      
      top = stack.pop();
    }
    
    boolean alpha_is_default = false;
    
    if (!(top instanceof Double)) {
      if (!(top instanceof Long)) {
        throw new WarpScriptException(getName() + " expects a significance level (a DOUBLE) or an upper bound of the number of outliers (a LONG) on top of the stack.");
      } else {
        alpha_is_default = true;
      }
    }
    
    if (!alpha_is_default) {
      params.put(SIGNIFICANCE_PARAM, ((Number) top).doubleValue());
      
      top = stack.pop();
    } else {
      params.put(SIGNIFICANCE_PARAM, SIGNIFICANCE_DEFAULT);
    }
    
    if (!(top instanceof Long)) {
      throw new WarpScriptException(getName() + " expects an upper bound of the number of outliers (a LONG) below the significance level.");
    }
    
    params.put(UPPERBOUND_PARAM, ((Number) top).intValue());
    
    top = stack.pop();
    
    if (!(top instanceof Long)) {
      throw new WarpScriptException(getName() + " expects a number of buckets per period (a LONG) below the upper bound of the number of outliers.");
    }
    
    params.put(PERIOD_PARAM, ((Number) top).intValue());
    
    return params;
  }

  @Override
  protected Object gtsOp(Map<String, Object> params, GeoTimeSerie gts) throws WarpScriptException {    
    int bpp = (int) params.remove(PERIOD_PARAM);
    int k = (int) params.remove(UPPERBOUND_PARAM);
    double alpha = (double) params.remove(SIGNIFICANCE_PARAM);
    
    return GTSOutliersHelper.STLESDTest(gts, bpp, k, alpha, params);
  }
}