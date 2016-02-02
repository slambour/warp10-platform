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

package io.warp10.script.mapper;

import io.warp10.continuum.gts.GeoTimeSerie;
import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptMapperFunction;
import io.warp10.script.WarpScriptException;

import java.util.Map;

/**
 * Mapper which returns the sigmoid of the value passed as parameter
 */
public class MapperSigmoid extends NamedWarpScriptFunction implements WarpScriptMapperFunction {

  public MapperSigmoid(String name) {
    super(name);
  }
  
  @Override
  public Object apply(Object[] args) throws WarpScriptException {
    long tick = (long) args[0];
    String[] names = (String[]) args[1];
    Map<String,String>[] labels = (Map<String,String>[]) args[2];
    long[] ticks = (long[]) args[3];
    long[] locations = (long[]) args[4];
    long[] elevations = (long[]) args[5];
    Object[] values = (Object[]) args[6];

    if (values.length > 1) {
      throw new WarpScriptException(getName() + " can only be applied to a single value.");
    } else if (0 == values.length) {
      return new Object[] { tick, GeoTimeSerie.NO_LOCATION, GeoTimeSerie.NO_ELEVATION, null };
    }
    
    Object value = null;
    long location = locations[0];
    long elevation = elevations[0];
        
    if (values[0] instanceof Long) {
      value = 1.0D / (1.0D + Math.exp(-1.0D * (long) values[0]));
    } else if (values[0] instanceof Double) {
      value = 1.0D / (1.0D + Math.exp(-1.0D * (double) values[0]));
    } else {
      throw new WarpScriptException(getName() + " can only be applied to LONG or DOUBLE values.");
    }
    
    return new Object[] { tick, location, elevation, value };
  }
}