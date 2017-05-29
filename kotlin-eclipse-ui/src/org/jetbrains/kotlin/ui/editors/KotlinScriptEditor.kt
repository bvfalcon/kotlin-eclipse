/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.ui.editors

import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.text.IDocument
import org.eclipse.swt.widgets.Composite
import org.eclipse.ui.PlatformUI
import org.jetbrains.kotlin.core.builder.KotlinPsiManager
import org.jetbrains.kotlin.core.model.KotlinScriptEnvironment
import org.jetbrains.kotlin.core.model.getEnvironment
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.script.KotlinScriptDefinitionProvider
import org.jetbrains.kotlin.script.KotlinScriptExternalDependencies
import org.jetbrains.kotlin.ui.editors.annotations.KotlinLineAnnotationsReconciler
import org.jetbrains.kotlin.script.ScriptDependenciesProvider
import kotlin.script.experimental.dependencies.ScriptDependencies

class KotlinScriptEditor : KotlinCommonEditor() {
    override val parsedFile: KtFile?
        get() {
            val file = eclipseFile ?: return null
            return KotlinPsiManager.getKotlinFileIfExist(file, document.get())
        }

    override val javaProject: IJavaProject? by lazy {
        eclipseFile?.let { JavaCore.create(it.getProject()) }
    }

    override val document: IDocument
        get() = getDocumentProvider().getDocument(getEditorInput())
    
    override fun createPartControl(parent: Composite) {
        super.createPartControl(parent)
        
        val file = eclipseFile ?: return
        val environment = getEnvironment(file) as KotlinScriptEnvironment

        environment.initializeScriptDefinitions { scriptDefinitions, classpath ->
            if (file.isAccessible && isOpen()) {
                KotlinScriptEnvironment.replaceEnvironment(file, scriptDefinitions, classpath)
                
                kotlinReconcilingStrategy.reconcile(null)
            }
        }
    }
    
    override val isScript: Boolean
        get() = true
    
    override fun dispose() {
        val file = eclipseFile
        if (file != null && file.exists()) {
            val family = KotlinScriptEnvironment.constructFamilyForInitialization(file);
            Job.getJobManager().cancel(family);
        }
        
        super.dispose()
        
        eclipseFile?.let {
            KotlinScriptEnvironment.removeKotlinEnvironment(it)
            KotlinPsiManager.removeFile(it)
        }
    }
}

private fun KotlinCommonEditor.isOpen(): Boolean {
    for (window in PlatformUI.getWorkbench().getWorkbenchWindows()) {
        for (page in window.getPages()) {
            for (editorReference in page.getEditorReferences()) {
                if (editorReference.getEditor(false) == this) {
                    return true
                }
            }
        }
    }
    
    return false
}