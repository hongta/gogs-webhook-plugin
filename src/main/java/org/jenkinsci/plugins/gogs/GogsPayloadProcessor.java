/*

The MIT License (MIT)
Copyright (c) 2016 Alexander Verhaar

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

package org.jenkinsci.plugins.gogs;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;
import hudson.model.Job;
import hudson.model.Cause;
import hudson.security.ACL;
import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import hudson.model.Cause;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

public class GogsPayloadProcessor {
  private static final Logger LOGGER = Logger.getLogger(GogsPayloadProcessor.class.getName());

  public GogsPayloadProcessor() {
  }

  public GogsResults triggerJobs(String jobName, String deliveryID) {
    Boolean didJob = false;
    GogsResults result = new GogsResults();

    SecurityContext old = Jenkins.getInstance().getACL().impersonate(ACL.SYSTEM);
    for (AbstractProject<?,?> project : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
      if ( project.getName().equals(jobName)) {

        Cause cause = new GogsCause(deliveryID);
        project.scheduleBuild(0, cause);
        didJob = true;
        result.Message = String.format("Job '%s' is executed",jobName);
      }
    }
    if (!didJob) {
      result.Status = 404;
      result.Message = String.format("Job '%s' is not defined in Jenkins",jobName);
      LOGGER.warning(result.Message);
    }
    SecurityContextHolder.setContext(old);

    return result;
  }
}
