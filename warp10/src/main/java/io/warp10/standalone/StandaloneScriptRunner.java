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

package io.warp10.standalone;

import io.warp10.continuum.BootstrapManager;
import io.warp10.continuum.Configuration;
import io.warp10.continuum.geo.GeoDirectoryClient;
import io.warp10.continuum.sensision.SensisionConstants;
import io.warp10.continuum.store.DirectoryClient;
import io.warp10.continuum.store.StoreClient;
import io.warp10.crypto.KeyStore;
import io.warp10.script.WarpScriptLib;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.MemoryWarpScriptStack;
import io.warp10.script.ScriptRunner;
import io.warp10.script.WarpScriptStack.StackContext;
import io.warp10.sensision.Sensision;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.RejectedExecutionException;

import com.google.common.base.Charsets;

public class StandaloneScriptRunner extends ScriptRunner {
  
  private final StoreClient storeClient;
  private final DirectoryClient directoryClient;
  private final GeoDirectoryClient geoDirectoryClient;
  private final Properties props;
  private final BootstrapManager bootstrapManager;
  
  public StandaloneScriptRunner(Properties properties, KeyStore keystore, StoreClient storeClient, DirectoryClient directoryClient, GeoDirectoryClient geoDirectoryClient, Properties props) throws IOException {
    super(keystore, props);
    
    this.props = props;
    this.directoryClient = directoryClient;
    this.geoDirectoryClient = geoDirectoryClient;
    this.storeClient = storeClient;
    
    //
    // Check if we have a 'bootstrap' property
    //
    
    if (properties.containsKey(Configuration.CONFIG_WARPSCRIPT_RUNNER_BOOTSTRAP_PATH)) {
      
      final String path = properties.getProperty(Configuration.CONFIG_WARPSCRIPT_RUNNER_BOOTSTRAP_PATH);
      
      long period = properties.containsKey(Configuration.CONFIG_WARPSCRIPT_RUNNER_BOOTSTRAP_PERIOD) ?  Long.parseLong(properties.getProperty(Configuration.CONFIG_WARPSCRIPT_RUNNER_BOOTSTRAP_PERIOD)) : Long.MAX_VALUE;
      this.bootstrapManager = new BootstrapManager(path, period);      
    } else {
      this.bootstrapManager = new BootstrapManager();
    }

  }
  
  @Override
  protected void schedule(Map<String, Long> nextrun, final String script, final long periodicity) {
    
    try {
      this.executor.submit(new Runnable() {            
        @Override
        public void run() {
          
          File f = new File(script);
          
          Map<String,String> labels = new HashMap<String,String>();
          //labels.put(SensisionConstants.SENSISION_LABEL_PATH, Long.toString(periodicity) + "/" + f.getName());
          labels.put(SensisionConstants.SENSISION_LABEL_PATH, f.getAbsolutePath().substring(getRoot().length() + 1));
          
          Sensision.update(SensisionConstants.SENSISION_CLASS_EINSTEIN_RUN_COUNT, labels, 1);

          long nano = System.nanoTime();
          
          WarpScriptStack stack = new MemoryWarpScriptStack(storeClient, directoryClient, geoDirectoryClient, props);

          
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          
          try {            
            Sensision.update(SensisionConstants.SENSISION_CLASS_EINSTEIN_RUN_CURRENT, Sensision.EMPTY_LABELS, 1);

            InputStream in = new FileInputStream(f);
                        
            byte[] buf = new byte[1024];
            
            while(true) {
              int len = in.read(buf);
              
              if (len < 0) {
                break;
              }
              
              baos.write(buf, 0, len);
            }
            
            // Add a 'CLEAR' at the end of the script so we don't return anything
            baos.write(CLEAR);
            
            in.close();

            //
            // Replace the context with the bootstrap one
            //
            
            StackContext context = bootstrapManager.getBootstrapContext();
                  
            if (null != context) {
              stack.push(context);
              stack.restore();
            }
                  
            //
            // Execute the bootstrap code
            //

            stack.exec(WarpScriptLib.BOOTSTRAP);

            stack.execMulti(new String(baos.toByteArray(), Charsets.UTF_8));
          } catch (Exception e) {                
            Sensision.update(SensisionConstants.SENSISION_CLASS_EINSTEIN_RUN_FAILURES, labels, 1);
          } finally {
            nano = System.nanoTime() - nano;
            Sensision.update(SensisionConstants.SENSISION_CLASS_EINSTEIN_RUN_TIME_US, labels, (long) (nano / 1000L));
            Sensision.update(SensisionConstants.SENSISION_CLASS_EINSTEIN_RUN_CURRENT, Sensision.EMPTY_LABELS, -1);
          }              
        }
      });                  
    } catch (RejectedExecutionException ree) {
      // Reschedule script immediately
      nextrun.put(script, System.currentTimeMillis());
    }    
  }
}