package lumien.randomthings.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

@Environment(EnvType.CLIENT)
public class PlantChestModel extends Model {

    private final ModelPart bottom;
    private final ModelPart lid;
    private final ModelPart lock;

    public PlantChestModel(ModelPart root) {
        super(RenderType::entitySolid);
        this.bottom = root.getChild("bottom");
        this.lid = root.getChild("lid");
        this.lock = this.lid.getChild("lock");
    }

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bottom",
            CubeListBuilder.create()
                .texOffs(0, 19)
                .addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F),
            PartPose.ZERO);

        PartDefinition lid = partdefinition.addOrReplaceChild("lid",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F),
            PartPose.offset(0.0F, 9.0F, 1.0F));

        lid.addOrReplaceChild("lock",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void setupAnim(float lidAngle) {
        this.lid.xRot = -(lidAngle * ((float) Math.PI / 2F));
        this.lock.xRot = this.lid.xRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
