package org.firstinspires.ftc.teamcode.examples.featuredev;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.teamcode.examples.featuredev.jdoc.BulkReads;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.dependency.Dependency;
import dev.frozenmilk.dairy.core.dependency.VoidDependency;
import dev.frozenmilk.dairy.core.dependency.annotation.AllAnnotations;
import dev.frozenmilk.dairy.core.dependency.annotation.AnnotationDependency;
import dev.frozenmilk.dairy.core.dependency.annotation.AnyAnnotations;
import dev.frozenmilk.dairy.core.dependency.annotation.OneOfAnnotations;
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation;
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotations;
import dev.frozenmilk.dairy.core.dependency.feature.AllFeatureClasses;
import dev.frozenmilk.dairy.core.dependency.feature.AllFeatures;
import dev.frozenmilk.dairy.core.dependency.feature.AnyFeatureClasses;
import dev.frozenmilk.dairy.core.dependency.feature.AnyFeatures;
import dev.frozenmilk.dairy.core.dependency.feature.FeatureDependency;
import dev.frozenmilk.dairy.core.dependency.feature.OneOfFeatureClasses;
import dev.frozenmilk.dairy.core.dependency.feature.OneOfFeatures;
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature;
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeatureClass;
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding;
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException;
import dev.frozenmilk.dairy.core.util.controller.Controller;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.util.cell.RefCell;
import kotlin.Pair;
import kotlin.Unit;

public class JavaDependencies {
	public JavaDependencies() {
		// All Dependencies share a root interface ancestor,
		// (This is an example constructed from a lambda)
		// Dependencies can return anything they want
		// and need to throw an exception to return early, and indicate that the operation has failed
		// commonly, this is the DependencyResolutionException
		// which is useful for listing pairs of objects and strings to associate with, to indicate reasons for failure
		// VoidDependency is for Java code, to remove the need for a return statement

		// VoidDependency is directly equivalent to Dependency<Unit>
		VoidDependency dependency = (
				// dependencies have access to:
				// the opMode wrapper
				Wrapper opMode,
				// the currently resolved features
				List<? extends Feature> resolvedFeatures,
				// and if the resolution process is currently yielding or not
				boolean yielding
		) -> {
			// this one just returns void, the equivalent of Kotlins's Unit
		};

		// receivers can be bound to the resolution of the dependency like so
		dependency.onResolve((Unit result) -> { // this gives us Kotlin's Unit type, which is the same as Java's void
			// so really there's nothing we can do with it
			// receive
		});

		// receivers can also be bound to the failed resolution of the dependency:
		dependency.onFail((Throwable exception) -> {
			// receive
		});

		// using the VoidDependency as a Lambda looks like this
		((VoidDependency) (opMode, resolvedFeatures, yielding1) -> {
		}).onResolve((result) -> {
			// receive
		});

		// Dairy's cells and other consumers can be used to extract directly from the binding,
		// although this might not always be perfect unless you have written a more customised dependency
		RefCell<Unit> cell = new RefCell<>(Unit.INSTANCE);
		// now when resolution succeeds, the cell will be receive to the result of resolution
		dependency.onResolve(cell);

		// a second example, this fails if the OpMode isn't teleop, and returns its name
		Dependency<String> dependency2 = (opMode, resolvedFeatures, yielding) -> {
			if(opMode.getOpModeType() != OpModeMeta.Flavor.TELEOP) throw new DependencyResolutionException("${opMode.opModeType} is not TELEOP");
			return opMode.getName();
		};

		// Kotlin offers infix functions to build compound dependencies
		// (this will just work, as they are the same condition)
		dependency.and(dependency2);
		// and and or are both short-circuiting, so take care to get the order right if you have side effects
		dependency.or(dependency2);

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
		Yielding yielding = Yielding.INSTANCE;

		// a common set of patterns are the yielding combinations

		// optional yield
		dependency.or(Yielding.INSTANCE); // will resolve after the first condition, or when you reach yielding

		// enforced yield
		dependency.and(Yielding.INSTANCE); // will resolved to always place after upgraded and Yielding

		// compounds end up with convoluted return types fast, so while it is possible,
		// it is not recommended to bind to compound endpoints beyond a single and/or bind
		// 'and' and 'or' are configured to respect their resolution strategies
		// when it comes time to supply to bound receivers

		dependency.and(dependency2)
			.onResolve((Pair<? extends Unit, ? extends String> pair) -> {
				Unit l = pair.component1();
				String r = pair.component2();
				// and will return a pair of both the left and right hand results
			});

		dependency.or(dependency2)
			.onResolve((Pair<? extends Unit, ? extends String> pair) -> {
				Unit l = pair.component1();
				String r = pair.component2();
				// while or will return:
				// (Object, null) if the left hand succeeded
				// or (null, Object) if the right hand succeeded
				// (Object, Object) will never be returned, as or is short-circuiting
		});

		//
		// Annotation Utils
		//

		// the AnnotationDependency base class can be used to quickly
		// write a custom dependency that analyses the annotations on the opmode
		new AnnotationDependency<>((annotations) -> {
			// filtering
			for (Annotation annotation : annotations) {
				if (annotation instanceof TeleOp) {
					return (TeleOp) annotation;
				}
			}
			// we need to throw a DependencyResolutionException if we cannot resolve, with a helpful message
			throw new DependencyResolutionException("OpMode does not have @TeleOp annotation");
		}).onResolve(teleOp -> {
			// now we can work with the @TeleOp Annotation easily!
			String name = teleOp.name();
		});

		// but many common applications have already been modelled

		// SingleAnnotation is the same as shown above
		new SingleAnnotation<>(TeleOp.class).onResolve((TeleOp teleop) -> {
			teleop.name();
		});

		// SingleAnnotation for annotations that can be applied multiple times
		new SingleAnnotations<>(TeleOp.class).onResolve((List<? extends TeleOp> annotations) -> {
			for (TeleOp teleOp : annotations) {
				teleOp.name();
			}
		});

		// AllAnnotations is for when you require all the annotations listed,
		// but don't necessarily care about each one
		// This collects one of each
		new AllAnnotations(TeleOp.class, JavaWritingAFeature.Attach.class).onResolve((List<? extends Annotation> annotations) -> {
			for (Annotation annotation : annotations) {
				if (annotation instanceof TeleOp) {
					((TeleOp) annotation).name();
				}
				if (annotation instanceof JavaWritingAFeature.Attach) {
					// no data can be extracted here
				}
			}
		});

		// AnyAnnotations is when multiple types are acceptable, it collects all that it can
		new AnyAnnotations(TeleOp.class, Autonomous.class).onResolve((List<? extends Annotation> annotations) -> {
			for (Annotation annotation : annotations) {
				if (annotation instanceof TeleOp) {
					((TeleOp) annotation).name();
				}
				if (annotation instanceof Autonomous) {
					((Autonomous) annotation).name();
				}
			}
		});

		// OneOfAnnotations is when multiple types are acceptable, but only one may be attached.
		// if more than one of the annotations appear, it will not resolve
		new OneOfAnnotations(TeleOp.class, Autonomous.class).onResolve((Annotation annotation) -> {
			if (annotation instanceof TeleOp) {
				((TeleOp) annotation).name();
			}
			if (annotation instanceof Autonomous) {
				((Autonomous) annotation).name();
			}
		});

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
		new FeatureDependency<>((features) -> {
			// filtering
			for (Feature feature : features) {
				if (feature instanceof BulkReads) {
					return (BulkReads) feature;
				}
			}
			// we need to throw a DependencyResolutionException if we cannot resolve, with a helpful message
			throw new DependencyResolutionException("BulkReads was not attached");
		}).onResolve((BulkReads bulkReads) -> {
			// we'll deregister BulkReads if we found it
			bulkReads.deregister();
			// then, we could register our own one instead!
		});

		// We have all the same options for annotations for features, but also for both instances and classes

		// SingleFeature is the same as shown above
		new SingleFeature<>(BulkReads.INSTANCE).onResolve((bulkReads) -> {
			// we'll deregister BulkReads if we found it
			bulkReads.deregister();
			// then, we could register our own one instead!
		});

		// The Class equivalent collects all features of the provided type
		// noinspection rawtypes
		new SingleFeatureClass<>(Controller.class).onResolve((List<? extends Controller> features) -> {
			// we could disable all the controllers that we find
			// this isn't amazing, as this is not guaranteed to find all Controllers
			// unless its attached later
			features.forEach(controller -> controller.setEnabled(false));
		});

		// AllFeatures ensures that all features are attached, then gives us back the set of them
		new AllFeatures(BulkReads.INSTANCE, JavaWritingAFeature.INSTANCE).onResolve((Set<? extends Feature> features) -> {
			for (Feature feature : features) {
				if (feature instanceof BulkReads) {
					feature.deregister();
				}
				if (feature instanceof JavaWritingAFeature) {
					// w/e
				}
			}
		});

		// the Class equivalent ensures that at least one Feature of each of the given exact classes is attached, and returns all that it found
		new AllFeatureClasses(BulkReads.class, JavaWritingAFeature.class).onResolve((List<? extends Feature> features) -> {
			for (Feature feature : features) {
				if (feature instanceof BulkReads) {
					feature.deregister();
				}
				if (feature instanceof JavaWritingAFeature) {
					// w/e
				}
			}
		});

		// Ensures that at least one of these is attached, and returns all that are
		new AnyFeatures(BulkReads.INSTANCE, JavaWritingAFeature.INSTANCE).onResolve((Set<? extends Feature> features) -> {
			for (Feature feature : features) {
				if (feature instanceof BulkReads) {
					feature.deregister();
				}
				if (feature instanceof JavaWritingAFeature) {
					// w/e
				}
			}
		});

		// The Class equivalent ensures that at least one Feature of the class types is attached, returns all that are
		new AnyFeatureClasses(BulkReads.class, JavaWritingAFeature.class).onResolve((List<? extends Feature> features) -> {
			for (Feature feature : features) {
				if (feature instanceof BulkReads) {
					feature.deregister();
				}
				if (feature instanceof JavaWritingAFeature) {
					// w/e
				}
			}
		});

		// Ensures that exactly one of the Features are attached, and returns it
		new OneOfFeatures(BulkReads.INSTANCE, JavaWritingAFeature.INSTANCE).onResolve((Feature feature) -> {
			if (feature instanceof BulkReads) {
				feature.deregister();
			}
			if (feature instanceof JavaWritingAFeature) {
				// w/e
			}
		});

		// Ensures that exactly one Feature of the exact classes is attached, and returns it
		new OneOfFeatureClasses(BulkReads.class, JavaWritingAFeature.class).onResolve((feature) -> {
			if (feature instanceof BulkReads) {
				feature.deregister();
			}
			if (feature instanceof JavaWritingAFeature) {
				// w/e
			}
		});

		// Once again,
		// note that many more complex behaviours can me modelled using these pre-written components,
		// and the boolean combinators

		// also note that some of these behaviours can be achieved using the simpler building blocks and
		// the boolean combinators. this may be preferable if you need to work with the result of the resolution
		// in the onResolve blocks
	}
}
