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
package org.jetbrains.kotlin.core.resolve

import org.jetbrains.kotlin.caches.resolve.KotlinCacheService
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.idea.resolve.ResolutionFacade
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.analyzer.AnalysisResult
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.core.builder.KotlinPsiManager
import org.eclipse.jdt.core.JavaCore
import org.jetbrains.kotlin.core.log.KotlinLogger
import org.jetbrains.kotlin.core.model.KotlinAnalysisFileCache
import org.jetbrains.kotlin.resolve.diagnostics.KotlinSuppressCache

public class KotlinCacheServiceImpl : KotlinCacheService {
    override fun getSuppressionCache(): KotlinSuppressCache {
        throw UnsupportedOperationException()
    }

    override fun getResolutionFacade(elements: List<KtElement>): ResolutionFacade {
        return KotlinSimpleResolutionFacade()
    }
}

class KotlinSimpleResolutionFacade : ResolutionFacade {
    override val moduleDescriptor: ModuleDescriptor
        get() = throw UnsupportedOperationException()
    
    override val project: Project
        get() = throw UnsupportedOperationException()
    
    override fun analyze(element: KtElement, bodyResolveMode: BodyResolveMode): BindingContext {
        val ktFile = element.getContainingKtFile()
        val javaProject = KotlinPsiManager.getJavaProject(element)
        if (javaProject == null) {
            KotlinLogger.logWarning("JavaProject for $element (in $ktFile) is null")
            return BindingContext.EMPTY
        }
        
        return KotlinAnalysisFileCache.getAnalysisResult(ktFile, javaProject).analysisResult.bindingContext
    }
    
    override fun analyzeFullyAndGetResult(elements: Collection<KtElement>): AnalysisResult {
        throw UnsupportedOperationException()
    }
    
    override fun <T : Any> getFrontendService(element: PsiElement, serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }
    
    override fun <T : Any> getFrontendService(serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }
    
    override fun <T : Any> getFrontendService(moduleDescriptor: ModuleDescriptor, serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }
    
    override fun <T : Any> getIdeService(serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }
    
    override fun resolveToDescriptor(declaration: KtDeclaration): DeclarationDescriptor {
        throw UnsupportedOperationException()
    }
}