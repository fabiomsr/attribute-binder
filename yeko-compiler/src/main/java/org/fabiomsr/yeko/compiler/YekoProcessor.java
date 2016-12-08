package org.fabiomsr.yeko.compiler;

import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Created by Fabiomsr on 25/7/16.
 */
@AutoService(Processor.class)
public class YekoProcessor extends AbstractProcessor {
  private YekoProcessorImpl yekoProcessorImpl;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    yekoProcessorImpl = new YekoProcessorImpl(new Logger(processingEnv.getMessager()),
                                              processingEnv.getFiler(),
                                              processingEnv.getElementUtils(),
                                              processingEnv.getOptions());


  }

  @Override
  public Set<String> getSupportedOptions() {
    return yekoProcessorImpl.getSupportedOptions();
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return yekoProcessorImpl.getSupportedSourceVersion();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return yekoProcessorImpl.getSupportedAnnotationTypes();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    return yekoProcessorImpl.process(annotations, roundEnv);
  }
}
