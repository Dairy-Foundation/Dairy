package org.firstinspires.ftc.teamcode.examples.featuredev

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.annotation.AllAnnotations
import dev.frozenmilk.dairy.core.dependency.annotation.AnnotationDependency
import dev.frozenmilk.dairy.core.dependency.annotation.AnyAnnotations
import dev.frozenmilk.dairy.core.dependency.annotation.OneOfAnnotations
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotations
import dev.frozenmilk.dairy.core.dependency.feature.AllFeatureClasses
import dev.frozenmilk.dairy.core.dependency.feature.AllFeatures
import dev.frozenmilk.dairy.core.dependency.feature.AnyFeatureClasses
import dev.frozenmilk.dairy.core.dependency.feature.AnyFeatures
import dev.frozenmilk.dairy.core.dependency.feature.FeatureDependency
import dev.frozenmilk.dairy.core.dependency.feature.OneOfFeatureClasses
import dev.frozenmilk.dairy.core.dependency.feature.OneOfFeatures
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeatureClass
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.mercurial.Mercurial
import dev.frozenmilk.mercurial.commands.LambdaCommand
import dev.frozenmilk.util.cell.RefCell
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

class KotlinDependencies {
	init {
		// All Dependencies share a root interface ancestor,
		// (This is an example constructed from a lambda)
		// Dependencies can return anything they want
		// and need to throw an exception to return early, and indicate that the operation has failed
		// commonly, this is the DependencyResolutionException
		// which is useful for listing pairs of objects and strings to associate with, to indicate reasons for failure
		val dependency1 = Dependency {
			// dependencies have access to:
			// the opMode wrapper
				opMode: Wrapper,
				// the currently resolved features
				resolvedFeatures: List<Feature>,
				// and if the resolution process is currently yielding or not
				yielding: Boolean ->
			// this one just returns Unit, the equivalent of java's void
		}

		// receivers can be bound to the resolution of the dependency like so
		dependency1.onResolve { result: Unit -> // in this case, our result is of type unit
			// receive
		}

		// receivers can also be bound to the failed resolution of the dependency:
		dependency1.onFail { exception: Throwable ->
			// receive
		}

		// Dairy's cells and other consumers can be used to extract directly from the binding,
		// although this might not always be perfect unless you have written a more customised dependency
		val cell = RefCell(Unit)
		// now when resolution succeeds, the cell will be receive to the result of resolution
		dependency1.onResolve(cell)

		// a second example, this fails if the OpMode isn't teleop
		val dependency2 = Dependency { opMode, resolvedFeatures, yielding ->
			if(opMode.opModeType != OpModeMeta.Flavor.TELEOP) throw DependencyResolutionException("${opMode.opModeType} is not TELEOP")
		}

		// kotlin offers infix functions to build compound dependencies
		dependency1 and dependency2
		// and and or are both short-circuiting, so take care to get the order right if you have side effects
		dependency1 or dependency2

		// Yielding is often used as the default 'lazy' dependency
		// Any dependency that Yields is generally considered less important than those that don't
		// Dependency resolution happens over several rounds
		// Yielding occurs after a round that resulted in no new resolutions
		// Most instantiated features just use Yielding, as it allows them  to sink to run after more important features
		// In comparison, most static instance features don't use Yielding,
		// as they are generally considered important
		// multiple rounds of yielding can occur,
		// as new resolutions from yielding rounds can then cause further new
		// resolutions in subsequent resolution rounds
		Yielding

		// a common set of patterns are the yielding combinations

		// optional yield
		dependency1 or Yielding // will resolve after the first condition, or when you reach yielding

		// enforced yield
		dependency1 and Yielding // will resolved to always place after upgraded and Yielding

		// compounds end up with convoluted return types fast, so while it is possible,
		// it is not recommended to bind to compound endpoints beyond a single and/or bind
		// 'and' and 'or' are configured to respect their resolution strategies
		// when it comes time to supply to bound receivers

		(dependency1 and dependency2)
			.onResolve { (l: Unit, r: Unit) ->
				// and will return a pair of both the left and right hand results
			}

		(dependency1 or dependency2)
			.onResolve { (l: Unit?, r: Unit?) ->
				// while or will return:
				// (Unit, null) if the left hand succeeded
				// or (null, Unit) if the right hand succeeded
				// (Unit, Unit) will never be returned, as or is short-circuiting
			}

		//
		// Annotation Utils
		//

		// the AnnotationDependency base class can be used to quickly
		// write a custom dependency that analyses the annotations on the opmode
		AnnotationDependency {
			// filtering
			it.filterIsInstance<TeleOp>().ifEmpty {
				// we need to throw a DependencyResolutionException if we cannot resolve, with a helpful message
				throw DependencyResolutionException("OpMode does not have @TeleOp annotation")
			}.first()
		}.onResolve {
			// now we can work with the @TeleOp Annotation easily!
			it.name
		}

		// but many common applications have already been modelled

		// SingleAnnotation is the same as shown above
		SingleAnnotation(TeleOp::class.java).onResolve {
			it.name
		}

		// SingleAnnotation for annotations that can be applied multiple times
		SingleAnnotations(TeleOp::class.java).onResolve { annotations ->
			annotations.forEach {
				it.name
			}
		}

		// AllAnnotations is for when you require all the annotations listed,
		// but don't necessarily care about each one
		// This collects one of each
		AllAnnotations(TeleOp::class.java, KotlinWritingAFeature.Attach::class.java).onResolve { annotations ->
			annotations.forEach {
				when(it) {
					is TeleOp -> {
						it.name
					}
					is KotlinWritingAFeature.Attach -> {
						// no data can be extracted here
					}
				}
			}
		}

		// AnyAnnotations is when multiple types are acceptable, it collects all that it can
		AnyAnnotations(TeleOp::class.java, Autonomous::class.java).onResolve { annotations ->
			annotations.forEach {
				when(it) {
					is TeleOp -> {
						it.name
					}
					is Autonomous -> {
						it.name
					}
				}
			}
		}

		// OneOfAnnotations is when multiple types are acceptable, but only one may be attached.
		// if more than one of the annotations appear, it will not resolve
		OneOfAnnotations(TeleOp::class.java, Autonomous::class.java).onResolve {
			when(it) {
				is TeleOp -> {
					it.name
				}
				is Autonomous -> {
					it.name
				}
			}
		}

		// note that many more complex behaviours can me modelled using these pre-written components,
		// and the boolean combinators

		// also note that some of these behaviours can be achieved using the simpler building blocks and
		// the boolean combinators. this may be preferable if you need to work with the result of the resolution
		// in the onResolve blocks

		//
		// Feature Utils
		//

		// the FeatureDependency base class can be used to quickly
		// write a custom dependency that analyses the currently resolved Features
		FeatureDependency { features ->
			features.filterIsInstance<Mercurial>()
				.ifEmpty {
					// we need to throw a DependencyResolutionException if we cannot resolve, with a helpful message
					throw DependencyResolutionException("Mercurial was not attached")
				}.first()
		}.onResolve {
			// we can schedule an extra startup command for Mercurial
			it.scheduleCommand(LambdaCommand())
		}

		// We have all the same options for annotations for features, but also for both instances and classes

		// SingleFeature is the same as shown above
		SingleFeature(Mercurial).onResolve {
			// we can schedule an extra startup command for Mercurial
			it.scheduleCommand(LambdaCommand())
		}

		// The Class equivalent collects all features of the provided type
		SingleFeatureClass(Mercurial::class.java).onResolve { features ->
			// we can schedule an extra startup command for Mercurial
			features.first().scheduleCommand(LambdaCommand())
		}

		// AllFeatures ensures that all features are attached, then gives us back the set of them
		AllFeatures(Mercurial, KotlinWritingAFeature).onResolve { features ->
			features.forEach {
				when(it) {
					is Mercurial -> {
						it.scheduleCommand(LambdaCommand())
					}
					is KotlinWritingAFeature -> {
						// w/e
					}
				}
			}
		}

		// the Class equivalent ensures that at least one Feature of each of the given exact classes is attached, and returns all that it found
		AllFeatureClasses(Mercurial::class.java, KotlinWritingAFeature::class.java).onResolve { features ->
			features.forEach {
				when(it) {
					is Mercurial -> {
						it.scheduleCommand(LambdaCommand())
					}
					is KotlinWritingAFeature -> {
						// w/e
					}
				}
			}
		}

		// Ensures that at least one of these is attached, and returns all that are
		AnyFeatures(Mercurial, KotlinWritingAFeature).onResolve { features ->
			features.forEach {
				when(it) {
					is Mercurial -> {
						it.scheduleCommand(LambdaCommand())
					}
					is KotlinWritingAFeature -> {
						// w/e
					}
				}
			}
		}

		// The Class equivalent ensures that at least one Feature of the class types is attached, returns all that are
		AnyFeatureClasses(Mercurial::class.java, KotlinWritingAFeature::class.java).onResolve { features ->
			features.forEach {
				when(it) {
					is Mercurial -> {
						it.scheduleCommand(LambdaCommand())
					}
					is KotlinWritingAFeature -> {
						// w/e
					}
				}
			}
		}

		// Ensures that exactly one of the Features are attached, and returns it
		OneOfFeatures(Mercurial, KotlinWritingAFeature).onResolve {
			when(it) {
				is Mercurial -> {
					it.scheduleCommand(LambdaCommand())
				}
				is KotlinWritingAFeature -> {
					// w/e
				}
			}
		}

		// Ensures that exactly one Feature of the exact classes is attached, and returns it
		OneOfFeatureClasses(Mercurial::class.java, KotlinWritingAFeature::class.java).onResolve {
			when(it) {
				is Mercurial -> {
					it.scheduleCommand(LambdaCommand())
				}
				is KotlinWritingAFeature -> {
					// w/e
				}
			}
		}

		// Once again,
		// note that many more complex behaviours can me modelled using these pre-written components,
		// and the boolean combinators

		// also note that some of these behaviours can be achieved using the simpler building blocks and
		// the boolean combinators. this may be preferable if you need to work with the result of the resolution
		// in the onResolve blocks
	}
}