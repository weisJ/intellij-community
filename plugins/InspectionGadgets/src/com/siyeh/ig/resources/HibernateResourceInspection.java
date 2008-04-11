/*
 * Copyright 2003-2008 Dave Griffith, Bas Leijdekkers
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
 */
package com.siyeh.ig.resources;

import com.intellij.psi.*;
import com.siyeh.HardcodedMethodConstants;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.psiutils.TypeUtils;
import org.jetbrains.annotations.NotNull;

public class HibernateResourceInspection extends ResourceInspection {

    @NotNull
    public String getID(){
        return "HibernateResourceOpenedButNotSafelyClosed";
    }

    @NotNull
    public String getDisplayName(){
        return InspectionGadgetsBundle.message(
                "hibernate.resource.opened.not.closed.display.name");
    }

    @NotNull
    public String buildErrorString(Object... infos){
        final PsiType type = (PsiType)infos[0];
        return InspectionGadgetsBundle.message(
                "hibernate.resource.opened.not.closed.problem.descriptor",
                type.getPresentableText());
    }

    public BaseInspectionVisitor buildVisitor(){
        return new HibernateResourceVisitor();
    }

    private static class HibernateResourceVisitor extends BaseInspectionVisitor{

        @Override public void visitMethodCallExpression(
                @NotNull PsiMethodCallExpression expression){
            super.visitMethodCallExpression(expression);
            if(!isHibernateFactoryMethod(expression)){
                return;
            }
            final PsiElement parent = getExpressionParent(expression);
            if(parent instanceof PsiReturnStatement){
                return;
            }
            final PsiVariable boundVariable = getVariable(parent);
            if(isSafelyClosed(boundVariable, expression)){
                return;
            }
            if(isResourceEscapedFromMethod(boundVariable, expression)){
                return;
            }
            registerError(expression, expression);
        }

        private static boolean isHibernateFactoryMethod(
                PsiMethodCallExpression expression){
            final PsiReferenceExpression methodExpression =
                    expression.getMethodExpression();
            final String methodName = methodExpression.getReferenceName();
            if(!HardcodedMethodConstants.OPEN_SESSION.equals(methodName)){
                return false;
            }
            final PsiExpression qualifier =
                    methodExpression.getQualifierExpression();
            if(qualifier == null){
                return false;
            }
            return TypeUtils.expressionHasTypeOrSubtype(qualifier,
                    "org.hibernate.SessionFactory");
        }
    }
}