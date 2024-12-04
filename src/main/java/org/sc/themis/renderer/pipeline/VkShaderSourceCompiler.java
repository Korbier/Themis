package org.sc.themis.renderer.pipeline;

import org.lwjgl.util.shaderc.Shaderc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class VkShaderSourceCompiler {

    final private static Logger logger = LoggerFactory.getLogger( VkShaderSourceCompiler.class );

    public static void compileShaderIfChanged(String glsShaderFile, int shaderType) {
        compileShaderIfChanged(glsShaderFile, glsShaderFile + ".spv", shaderType);
    }

    public static void compileShaderIfChanged(String glsShaderFile, String output, int shaderType) {

        byte[] compiledShader;

        try {

            File glslFile = new File(glsShaderFile);
            File spvFile  = new File(output);

            spvFile.getParentFile().mkdirs();

            if ( !spvFile.exists() || glslFile.lastModified() > spvFile.lastModified() ) {

                if ( !glslFile.exists() ) {
                    logger.debug("Shader {} does not exists", glslFile.getPath() );
                }

                logger.debug("Compiling {} to {}", glslFile.getPath(), spvFile.getPath());
                String shaderCode = new String(Files.readAllBytes(glslFile.toPath()));
                compiledShader = compileShader(shaderCode, shaderType);
                Files.write(spvFile.toPath(), compiledShader);

            } else {
                logger.debug("Shader {} already compiled. Loading compiled version: {}", glslFile.getPath(), spvFile.getPath());
            }

        } catch (IOException excp) {
            throw new RuntimeException(excp);
        }

    }

    public static byte[] compileShader(String shaderCode, int shaderType) {

        long compiler = 0;
        long options = 0;
        byte[] compiledShader;

        try {
            compiler = Shaderc.shaderc_compiler_initialize();
            options  = Shaderc.shaderc_compile_options_initialize();

            long result = Shaderc.shaderc_compile_into_spv(
                    compiler,
                    shaderCode, shaderType,
                    "shader.glsl", "main",
                    options
            );

            if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
                throw new RuntimeException("Shader compilation failed: " + Shaderc.shaderc_result_get_error_message(result));
            }

            ByteBuffer buffer = Shaderc.shaderc_result_get_bytes(result);
            compiledShader = new byte[buffer.remaining()];
            buffer.get(compiledShader);

        } finally {
            Shaderc.shaderc_compile_options_release(options);
            Shaderc.shaderc_compiler_release(compiler);
        }

        return compiledShader;

    }

}
