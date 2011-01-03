/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.selenium.impl;

import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.Before;

/**
 * A handler which sets a cached instance of Selenium browser for fields
 * annotated with {@link Selenium}. <br/>
 * <b>Imports:</b><br/> {@link Selenium} <br/> {@link SeleniumHolder} <br/>
 * <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see SeleniumHolder
 * @see Selenium
 */
public class SeleniumInjector
{
   @Inject
   private Instance<SeleniumHolder> selenium;

   public void injectSelenium(@Observes Before event)
   {
      Class<?> clazz = event.getTestClass().getJavaClass();
      Object testInstance = event.getTestInstance();

      List<Field> fields = SecurityActions.getFieldsWithAnnotation(clazz, Selenium.class);
      try
      {
         for (Field f : fields)
         {
            f.setAccessible(true);

            // omit setting if already set
            if (f.get(testInstance) != null)
            {
               return;
            }

            Object value = selenium.get().retrieveSelenium(f.getType());
            if (value == null)
            {
               throw new IllegalArgumentException("Retrieved a null from context, which is not a valid Selenium browser");
            }

            f.set(testInstance, value);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not inject Selenium members", e);
      }
   }
}