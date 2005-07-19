package org.apache.maven.artifact.versioning;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Construct a version range from a specification.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class VersionRange
{
    private final String recommendedVersion;

    private final List restrictions;

    private VersionRange( String recommendedVersion, List restrictions )
    {
        this.recommendedVersion = recommendedVersion;
        this.restrictions = restrictions;
    }

    public String getRecommendedVersion()
    {
        return recommendedVersion;
    }

    public List getRestrictions()
    {
        return restrictions;
    }

    public static VersionRange createFromVersionSpec( String spec )
        throws InvalidVersionSpecificationException
    {
        List exclusions = new ArrayList();
        String process = spec;
        String version = null;

        while ( process.startsWith( "[" ) || process.startsWith( "(" ) )
        {
            int index1 = process.indexOf( ")" );
            int index2 = process.indexOf( "]" );

            int index = index2;
            if ( index2 < 0 || index1 < index2 )
            {
                if ( index1 >= 0 )
                {
                    index = index1;
                }
            }

            if ( index < 0 )
            {
                throw new InvalidVersionSpecificationException( "Unbounded range: " + spec );
            }

            exclusions.add( parseRestriction( process.substring( 0, index + 1 ) ) );

            process = process.substring( index + 1 ).trim();

            if ( process.length() > 0 && process.startsWith( "," ) )
            {
                process = process.substring( 1 ).trim();
            }
        }

        if ( process.length() > 0 )
        {
            if ( exclusions.size() > 0 )
            {
                throw new InvalidVersionSpecificationException(
                    "Only fully-qualified sets allowed in multiple set scenario: " + spec );
            }
            else
            {
                version = process;
            }
        }

        return new VersionRange( version, exclusions );
    }

    private static Restriction parseRestriction( String spec )
        throws InvalidVersionSpecificationException
    {
        boolean lowerBoundInclusive = spec.startsWith( "[" );
        boolean upperBoundInclusive = spec.endsWith( "]" );

        String process = spec.substring( 1, spec.length() - 1 ).trim();

        Restriction restriction;

        int index = process.indexOf( "," );

        if ( index < 0 )
        {
            if ( !lowerBoundInclusive || !upperBoundInclusive )
            {
                throw new InvalidVersionSpecificationException( "Single version must be surrounded by []: " + spec );
            }
            restriction = new Restriction( process, lowerBoundInclusive, process, upperBoundInclusive );
        }
        else
        {
            String lowerBound = process.substring( 0, index ).trim();
            String upperBound = process.substring( index + 1 ).trim();
            if ( lowerBound.equals( upperBound ) )
            {
                throw new InvalidVersionSpecificationException( "Range cannot have identical boundaries: " + spec );
            }

            if ( lowerBound.length() == 0 )
            {
                lowerBound = null;
            }
            if ( upperBound.length() == 0 )
            {
                upperBound = null;
            }

            restriction = new Restriction( lowerBound, lowerBoundInclusive, upperBound, upperBoundInclusive );
        }

        return restriction;
    }

    public static VersionRange createFromVersion( String version )
    {
        return new VersionRange( version, Collections.EMPTY_LIST );
    }
}
