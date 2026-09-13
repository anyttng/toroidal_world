package com.toroidalworld.engine.net;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class RecordPayloadFold {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Set<Type> POSITION_TYPES =
            Set.of(BlockPos.class, Vec3.class, ChunkPos.class, SectionPos.class, GlobalPos.class);

    private static final Set<Type> CONTAINER_TYPES = Set.of(Optional.class, List.class);

    private static final MethodType ACCESSOR_TYPE = MethodType.methodType(Object.class, Object.class);

    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(Object.class, Object[].class);

    private static final ClassValue<Form> FORMS = new ClassValue<>() {
        @Override
        protected Form computeValue(Class<?> type) {
            return Form.of(type);
        }
    };

    private static volatile Set<ResourceLocation> denied = Set.of();

    static void deny(Set<ResourceLocation> payloadIds) {
        denied = Set.copyOf(payloadIds);
    }

    static CustomPacketPayload toClient(CustomPacketPayload payload, TranslationContext context) {
        return folded(payload, context, FoldedValue::toClient);
    }

    static CustomPacketPayload toServer(CustomPacketPayload payload, TranslationContext context) {
        return folded(payload, context, FoldedValue::toServer);
    }

    static Form formOf(Class<?> payloadType) {
        return FORMS.get(payloadType);
    }

    private static CustomPacketPayload folded(CustomPacketPayload payload, TranslationContext context,
            Function<TranslationContext, FoldedValue.Leaves> leavesOf) {
        Form form = FORMS.get(payload.getClass());
        if (!form.carriesPositions() || denied.contains(payload.type().id())) {
            return payload;
        }

        FoldedValue.Leaves leaves = leavesOf.apply(context);
        return form.rebuilt(payload, value -> FoldedValue.walk(context, leaves, value));
    }

    private static boolean carriesPosition(Type type) {
        if (type instanceof ParameterizedType parameterized && CONTAINER_TYPES.contains(parameterized.getRawType())) {
            return POSITION_TYPES.contains(parameterized.getActualTypeArguments()[0]);
        }

        return POSITION_TYPES.contains(type);
    }

    static final class Form {
        private static final Form POSITIONLESS = new Form(null, new MethodHandle[0], new boolean[0]);

        private final @Nullable MethodHandle constructor;

        private final MethodHandle[] accessors;

        private final boolean[] positions;

        private final AtomicBoolean refusalLogged = new AtomicBoolean();

        private Form(@Nullable MethodHandle constructor, MethodHandle[] accessors, boolean[] positions) {
            this.constructor = constructor;
            this.accessors = accessors;
            this.positions = positions;
        }

        boolean carriesPositions() {
            return constructor != null;
        }

        private static Form of(Class<?> type) {
            if (!type.isRecord()) {
                return POSITIONLESS;
            }

            RecordComponent[] components = type.getRecordComponents();
            boolean[] positions = new boolean[components.length];
            boolean carries = false;
            for (int index = 0; index < components.length; index++) {
                positions[index] = carriesPosition(components[index].getGenericType());
                carries |= positions[index];
            }

            if (!carries) {
                return POSITIONLESS;
            }

            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup());
                MethodHandle[] accessors = new MethodHandle[components.length];
                Class<?>[] parameterTypes = new Class<?>[components.length];
                for (int index = 0; index < components.length; index++) {
                    accessors[index] = lookup.unreflect(components[index].getAccessor()).asType(ACCESSOR_TYPE);
                    parameterTypes[index] = components[index].getType();
                }

                MethodHandle constructor = lookup.findConstructor(type, MethodType.methodType(void.class, parameterTypes))
                        .asSpreader(Object[].class, components.length)
                        .asType(CONSTRUCTOR_TYPE);
                return new Form(constructor, accessors, positions);
            } catch (IllegalAccessException | NoSuchMethodException e) {
                LOGGER.warn("Record payload {} carries a world position, but its components cannot be reached ({});"
                        + " its positions cross the seam unfolded", type.getName(), e.toString());
                return POSITIONLESS;
            }
        }

        private CustomPacketPayload rebuilt(CustomPacketPayload payload, UnaryOperator<Object> fold) {
            MethodHandle canonical = constructor;
            if (canonical == null) {
                return payload;
            }

            try {
                Object[] values = new Object[accessors.length];
                boolean changed = false;
                for (int index = 0; index < accessors.length; index++) {
                    Object value = (Object) accessors[index].invokeExact((Object) payload);
                    if (positions[index] && value != null) {
                        Object foldedValue = fold.apply(value);
                        changed |= foldedValue != value;
                        value = foldedValue;
                    }

                    values[index] = value;
                }

                if (!changed) {
                    return payload;
                }

                Object rebuilt = (Object) canonical.invokeExact(values);
                return (CustomPacketPayload) rebuilt;
            } catch (Error error) {
                throw error;
            } catch (Throwable refusal) {
                if (refusalLogged.compareAndSet(false, true)) {
                    LOGGER.warn("Record payload {} refused its folded positions ({}); it crosses the seam unfolded",
                            payload.getClass().getName(), refusal.toString());
                }

                return payload;
            }
        }
    }

    private RecordPayloadFold() {
    }
}
