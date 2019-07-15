// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.daemon.quickFix;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 * @deprecated Use {@link CreateDirectoryFix} or {@link CreateFileWithScopeFix} instead.
*/
@Deprecated
public class CreateFileFix extends AbstractCreateFileFix {
  private static final int REFRESH_INTERVAL = 1000;

  private final boolean myIsDirectory;
  private final String myNewFileName;
  private final String myText;
  @NotNull private final String myKey;
  private boolean myIsAvailable;
  private long myIsAvailableTimeStamp;

  // invoked from other module
  @SuppressWarnings("WeakerAccess")
  public CreateFileFix(boolean isDirectory,
                       @NotNull String newFileName,
                       @NotNull PsiDirectory directory,
                       @Nullable String text,
                       @NotNull String key) {
    super(directory);

    myIsDirectory = isDirectory;
    myNewFileName = newFileName;
    myText = text;
    myKey = key;
    myIsAvailable = isDirectory || !FileTypeManager.getInstance().getFileTypeByFileName(newFileName).isBinary();
    myIsAvailableTimeStamp = System.currentTimeMillis();
  }

  public CreateFileFix(@NotNull String newFileName, @NotNull PsiDirectory directory, String text) {
    this(false, newFileName, directory, text, "create.file.text");
  }

  public CreateFileFix(final boolean isDirectory, @NotNull String newFileName, @NotNull PsiDirectory directory) {
    this(isDirectory, newFileName, directory, null, isDirectory ? "create.directory.text" : "create.file.text");
  }

  @Override
  @Nullable
  protected String getFileText() {
    return myText;
  }

  @Override
  @NotNull
  public String getText() {
    return CodeInsightBundle.message(myKey, myNewFileName);
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return CodeInsightBundle.message("create.file.family");
  }

  @Nullable
  @Override
  public PsiElement getElementToMakeWritable(@NotNull PsiFile file) {
    return null;
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull PsiFile file,
                     Editor editor,
                     @NotNull PsiElement startElement,
                     @NotNull PsiElement endElement) {
    if (isAvailable(project, null, file)) {
      invoke(project, (PsiDirectory)startElement);
    }
  }

  @Override
  public void applyFix() {
    invoke(myStartElement.getProject(), (PsiDirectory)myStartElement.getElement());
  }

  @Override
  public boolean isAvailable(@NotNull Project project,
                             @NotNull PsiFile file,
                             @NotNull PsiElement startElement,
                             @NotNull PsiElement endElement) {
    final PsiDirectory myDirectory = (PsiDirectory)startElement;
    long current = System.currentTimeMillis();

    if (ApplicationManager.getApplication().isUnitTestMode() || current - myIsAvailableTimeStamp > REFRESH_INTERVAL) {
      myIsAvailable &= myDirectory.getVirtualFile().findChild(myNewFileName) == null;
      myIsAvailableTimeStamp = current;
    }

    return myIsAvailable;
  }

  private void invoke(@NotNull Project project, PsiDirectory myDirectory) throws IncorrectOperationException {
    myIsAvailableTimeStamp = 0; // to revalidate applicability

    try {
      if (myIsDirectory) {
        myDirectory.createSubdirectory(myNewFileName);
      }
      else {
        createFile(project, myDirectory, myNewFileName);
      }
    }
    catch (IncorrectOperationException e) {
      myIsAvailable = false;
    }
  }
}
